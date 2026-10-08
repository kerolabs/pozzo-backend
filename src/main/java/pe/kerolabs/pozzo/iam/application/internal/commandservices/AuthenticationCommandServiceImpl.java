package pe.kerolabs.pozzo.iam.application.internal.commandservices;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.iam.application.commandservices.AuthenticatedAccount;
import pe.kerolabs.pozzo.iam.application.commandservices.AuthenticationCommandService;
import pe.kerolabs.pozzo.iam.application.commandservices.CodeVerification;
import pe.kerolabs.pozzo.iam.application.commandservices.RecoveryCodeRequest;
import pe.kerolabs.pozzo.iam.application.commandservices.RecoveryVerification;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.email.EmailSender;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.RecoveryCode;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Session;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.commands.CompleteRegistrationCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RecoverAccountCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestRecoveryCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestRecoveryPhoneCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.SignOutCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.VerifyCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.VerifyRecoveryCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Profile;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.SessionTokenClaims;
import pe.kerolabs.pozzo.iam.domain.repositories.AccountRepository;
import pe.kerolabs.pozzo.iam.domain.repositories.RecoveryCodeRepository;
import pe.kerolabs.pozzo.iam.domain.repositories.SessionRepository;
import pe.kerolabs.pozzo.iam.domain.services.CodeGenerationService;
import pe.kerolabs.pozzo.iam.domain.services.TokenService;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Orchestrates passwordless access: request a code, verify it, complete the registration, sign out,
 * and recover an account with the backup email when the member lost their phone number.
 */
@Service
@Transactional
public class AuthenticationCommandServiceImpl implements AuthenticationCommandService {

    /** How long a new member has to complete the registration after verifying the number. */
    static final Duration REGISTRATION_TOKEN_VALIDITY = Duration.ofMinutes(15);

    /** How long a member has to link a new number after proving the account with the backup email. */
    static final Duration RECOVERY_TOKEN_VALIDITY = Duration.ofMinutes(15);

    private final AccountRepository accountRepository;
    private final SessionRepository sessionRepository;
    private final RecoveryCodeRepository recoveryCodeRepository;
    private final CodeGenerationService codeGenerationService;
    private final TokenService tokenService;
    private final PhoneCodes phoneCodes;
    private final EmailSender emailSender;
    private final Clock clock;

    public AuthenticationCommandServiceImpl(AccountRepository accountRepository,
                                            SessionRepository sessionRepository,
                                            RecoveryCodeRepository recoveryCodeRepository,
                                            CodeGenerationService codeGenerationService,
                                            TokenService tokenService,
                                            PhoneCodes phoneCodes,
                                            EmailSender emailSender,
                                            Clock clock) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.codeGenerationService = codeGenerationService;
        this.tokenService = tokenService;
        this.phoneCodes = phoneCodes;
        this.emailSender = emailSender;
        this.clock = clock;
    }

    @Override
    public Result<VerificationCode, ApplicationError> handle(RequestCodeCommand command) {
        return phoneCodes.issue(command.phoneNumber(), clock.instant());
    }

    @Override
    public Result<CodeVerification, ApplicationError> handle(VerifyCodeCommand command) {
        var now = clock.instant();
        return phoneCodes.verify(command.phoneNumber(), command.code(), now)
                .flatMap(phoneNumber -> signInOrAskForRegistration(phoneNumber, command.deviceLabel(), now));
    }

    @Override
    public Result<AuthenticatedAccount, ApplicationError> handle(CompleteRegistrationCommand command) {
        var now = clock.instant();

        var phoneNumber = tokenService.readRegistrationToken(command.registrationToken());
        if (phoneNumber.isEmpty()) {
            return Result.failure(ApplicationError.unauthorized(
                    "INVALID_REGISTRATION_TOKEN", "The registration token is invalid or has expired"));
        }
        if (!command.termsAccepted()) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "TERMS_NOT_ACCEPTED",
                    "The Terms and Conditions and the Privacy Policy must be accepted"));
        }
        if (accountRepository.existsByPhoneNumber(phoneNumber.get())) {
            return Result.failure(ApplicationError.conflict(
                    "Account", "The phone number already has an account"));
        }

        var account = accountRepository.save(Account.register(
                phoneNumber.get(), Profile.of(command.displayName(), command.photoUrl()), true, now));
        return Result.success(openSession(account, command.deviceLabel(), now));
    }

    @Override
    public Result<Session, ApplicationError> handle(SignOutCommand command) {
        var now = clock.instant();
        return sessionRepository.findById(command.sessionId())
                .filter(session -> session.getAccountId().equals(command.accountId()))
                .<Result<Session, ApplicationError>>map(session -> {
                    session.revoke(now);
                    return Result.success(sessionRepository.save(session));
                })
                .orElseGet(() -> Result.failure(ApplicationError.notFound("Session", command.sessionId().toString())));
    }

    @Override
    public Result<RecoveryCodeRequest, ApplicationError> handle(RequestRecoveryCodeCommand command) {
        var now = clock.instant();
        var email = command.email().strip().toLowerCase();

        var latest = recoveryCodeRepository.findLatestByEmail(email);
        if (latest.isPresent() && !latest.get().canBeReplacedAt(now)) {
            return Result.failure(ApplicationError.tooManyRequests(
                    "VERIFICATION_CODE_RESEND_TOO_SOON",
                    "A new code can be requested from %s".formatted(latest.get().resendAvailableAt())));
        }

        // The answer is the same whether the email has an account or not, so nobody can find out
        // which emails are registered; only an existing account receives the code.
        var account = accountRepository.findActiveByBackupEmail(email);
        if (account.isEmpty()) {
            return Result.success(new RecoveryCodeRequest(
                    email, now.plus(RecoveryCode.VALIDITY), now.plus(RecoveryCode.RESEND_COOLDOWN)));
        }

        recoveryCodeRepository.findAllPendingByEmail(email).forEach(pending -> {
            pending.invalidate();
            recoveryCodeRepository.save(pending);
        });
        var code = codeGenerationService.generate();
        emailSender.send(email, "Tu código para recuperar tu cuenta de Pozzo",
                ("Hola, %s:%n%nTu código para recuperar tu cuenta de Pozzo es %s. Vence en %d minutos.%n%n"
                        + "Si no lo pediste, ignora este correo: tu cuenta sigue igual.")
                        .formatted(account.get().getProfile().displayName(), code, RecoveryCode.VALIDITY.toMinutes()));
        var issued = recoveryCodeRepository.save(
                RecoveryCode.issue(account.get().getId(), email, codeGenerationService.hash(code), now));
        return Result.success(new RecoveryCodeRequest(email, issued.getExpiresAt(), issued.resendAvailableAt()));
    }

    @Override
    public Result<RecoveryVerification, ApplicationError> handle(VerifyRecoveryCodeCommand command) {
        var now = clock.instant();
        var email = command.email().strip().toLowerCase();

        var pending = recoveryCodeRepository.findPendingByEmail(email);
        if (pending.isEmpty()) {
            return Result.failure(ApplicationError.unauthorized(
                    "VERIFICATION_CODE_NOT_REQUESTED", "There is no pending code for this email"));
        }

        var recoveryCode = pending.get();
        var outcome = recoveryCode.verify(command.code(), now,
                input -> codeGenerationService.matches(input, recoveryCode.getCodeHash()));
        recoveryCodeRepository.save(recoveryCode);

        return switch (outcome) {
            case INVALID -> Result.failure(ApplicationError.unauthorized(
                    "INVALID_VERIFICATION_CODE", "%d attempts left".formatted(recoveryCode.remainingAttempts())));
            case EXPIRED -> Result.failure(ApplicationError.unauthorized(
                    "EXPIRED_VERIFICATION_CODE", "The code is no longer valid"));
            case BLOCKED -> Result.failure(ApplicationError.unauthorized(
                    "BLOCKED_VERIFICATION_CODE", "The code ran out of attempts"));
            case VERIFIED -> {
                var expiresAt = now.plus(RECOVERY_TOKEN_VALIDITY);
                var token = tokenService.issueRecoveryToken(recoveryCode.getAccountId(), now, expiresAt);
                yield Result.success(new RecoveryVerification(token, expiresAt));
            }
        };
    }

    @Override
    public Result<VerificationCode, ApplicationError> handle(RequestRecoveryPhoneCodeCommand command) {
        var now = clock.instant();
        return recoveringAccount(command.recoveryToken())
                .flatMap(account -> requireNumberFree(command.phoneNumber(), account.getId()))
                .flatMap(phoneNumber -> phoneCodes.issue(phoneNumber, now));
    }

    @Override
    public Result<AuthenticatedAccount, ApplicationError> handle(RecoverAccountCommand command) {
        var now = clock.instant();
        return recoveringAccount(command.recoveryToken())
                .flatMap(account -> requireNumberFree(command.phoneNumber(), account.getId())
                        .flatMap(phoneNumber -> phoneCodes.verify(phoneNumber, command.code(), now))
                        .map(phoneNumber -> {
                            // Whoever has the lost phone must not stay signed in.
                            sessionRepository.findAllNotRevokedByAccountId(account.getId()).forEach(session -> {
                                session.revoke(now);
                                sessionRepository.save(session);
                            });
                            account.changePhoneNumber(phoneNumber, true, now);
                            return openSession(accountRepository.save(account), command.deviceLabel(), now);
                        }));
    }

    private Result<Account, ApplicationError> recoveringAccount(String recoveryToken) {
        return tokenService.readRecoveryToken(recoveryToken)
                .flatMap(accountRepository::findById)
                .filter(Account::isActive)
                .<Result<Account, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(ApplicationError.unauthorized(
                        "INVALID_RECOVERY_TOKEN", "The recovery token is invalid or has expired")));
    }

    private Result<PhoneNumber, ApplicationError> requireNumberFree(PhoneNumber phoneNumber, UUID accountId) {
        var owner = accountRepository.findByPhoneNumber(phoneNumber);
        if (owner.isPresent() && !owner.get().getId().equals(accountId)) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "PHONE_NUMBER_IN_USE", "The phone number belongs to another account"));
        }
        return Result.success(phoneNumber);
    }

    private Result<CodeVerification, ApplicationError> signInOrAskForRegistration(
            PhoneNumber phoneNumber, @Nullable String deviceLabel, Instant now) {
        var account = accountRepository.findByPhoneNumber(phoneNumber);
        if (account.isEmpty()) {
            var expiresAt = now.plus(REGISTRATION_TOKEN_VALIDITY);
            var registrationToken = tokenService.issueRegistrationToken(phoneNumber, now, expiresAt);
            return Result.success(CodeVerification.registrationRequired(registrationToken, expiresAt));
        }
        if (!account.get().isActive()) {
            return Result.failure(ApplicationError.forbidden(
                    "ACCOUNT_DEACTIVATED", "The account of this phone number is deactivated"));
        }
        return Result.success(CodeVerification.signedIn(openSession(account.get(), deviceLabel, now)));
    }

    private AuthenticatedAccount openSession(Account account, @Nullable String deviceLabel, Instant now) {
        var sessionId = UUID.randomUUID();
        var expiresAt = now.plus(Session.VALIDITY);
        var token = tokenService.issueSessionToken(new SessionTokenClaims(account.getId(), sessionId), now, expiresAt);
        sessionRepository.save(Session.open(
                sessionId, account.getId(), tokenService.hash(token), deviceLabel, now, expiresAt));
        return new AuthenticatedAccount(account, token, expiresAt);
    }
}
