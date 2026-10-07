package pe.kerolabs.pozzo.iam.application.internal.commandservices;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.iam.application.commandservices.AuthenticatedAccount;
import pe.kerolabs.pozzo.iam.application.commandservices.AuthenticationCommandService;
import pe.kerolabs.pozzo.iam.application.commandservices.CodeVerification;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.SmsSender;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.TestPhoneNumbers;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Session;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.commands.CompleteRegistrationCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.SignOutCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.VerifyCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Profile;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.SessionTokenClaims;
import pe.kerolabs.pozzo.iam.domain.repositories.AccountRepository;
import pe.kerolabs.pozzo.iam.domain.repositories.SessionRepository;
import pe.kerolabs.pozzo.iam.domain.repositories.VerificationCodeRepository;
import pe.kerolabs.pozzo.iam.domain.services.CodeGenerationService;
import pe.kerolabs.pozzo.iam.domain.services.TokenService;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Orchestrates passwordless access: request a code, verify it, complete the registration and sign out.
 */
@Service
@Transactional
public class AuthenticationCommandServiceImpl implements AuthenticationCommandService {

    /** How long a new member has to complete the registration after verifying the number. */
    static final Duration REGISTRATION_TOKEN_VALIDITY = Duration.ofMinutes(15);

    private final VerificationCodeRepository verificationCodeRepository;
    private final AccountRepository accountRepository;
    private final SessionRepository sessionRepository;
    private final CodeGenerationService codeGenerationService;
    private final TokenService tokenService;
    private final SmsSender smsSender;
    private final TestPhoneNumbers testPhoneNumbers;
    private final Clock clock;

    public AuthenticationCommandServiceImpl(VerificationCodeRepository verificationCodeRepository,
                                            AccountRepository accountRepository,
                                            SessionRepository sessionRepository,
                                            CodeGenerationService codeGenerationService,
                                            TokenService tokenService,
                                            SmsSender smsSender,
                                            TestPhoneNumbers testPhoneNumbers,
                                            Clock clock) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.codeGenerationService = codeGenerationService;
        this.tokenService = tokenService;
        this.smsSender = smsSender;
        this.testPhoneNumbers = testPhoneNumbers;
        this.clock = clock;
    }

    @Override
    public Result<VerificationCode, ApplicationError> handle(RequestCodeCommand command) {
        var now = clock.instant();
        var phoneNumber = command.phoneNumber();

        var latest = verificationCodeRepository.findLatestByPhoneNumber(phoneNumber);
        if (latest.isPresent() && !latest.get().canBeReplacedAt(now)) {
            return Result.failure(ApplicationError.tooManyRequests(
                    "VERIFICATION_CODE_RESEND_TOO_SOON",
                    "A new code can be requested from %s".formatted(latest.get().resendAvailableAt())));
        }

        verificationCodeRepository.findAllPendingByPhoneNumber(phoneNumber).forEach(pending -> {
            pending.invalidate();
            verificationCodeRepository.save(pending);
        });

        var fixedCode = testPhoneNumbers.fixedCodeFor(phoneNumber);
        var code = fixedCode.orElseGet(codeGenerationService::generate);
        var issued = verificationCodeRepository.save(
                VerificationCode.issue(phoneNumber, codeGenerationService.hash(code), now));
        if (fixedCode.isEmpty()) {
            smsSender.send(phoneNumber, "Tu código de Pozzo es %s. Vence en %d minutos."
                    .formatted(code, VerificationCode.VALIDITY.toMinutes()));
        }
        return Result.success(issued);
    }

    @Override
    public Result<CodeVerification, ApplicationError> handle(VerifyCodeCommand command) {
        var now = clock.instant();
        var phoneNumber = command.phoneNumber();

        var pending = verificationCodeRepository.findPendingByPhoneNumber(phoneNumber);
        if (pending.isEmpty()) {
            return Result.failure(ApplicationError.unauthorized(
                    "VERIFICATION_CODE_NOT_REQUESTED", "There is no pending code for this phone number"));
        }

        var verificationCode = pending.get();
        var outcome = verificationCode.verify(command.code(), now, codeGenerationService);
        verificationCodeRepository.save(verificationCode);

        return switch (outcome) {
            case INVALID -> Result.failure(ApplicationError.unauthorized(
                    "INVALID_VERIFICATION_CODE",
                    "%d attempts left".formatted(verificationCode.remainingAttempts())));
            case EXPIRED -> Result.failure(ApplicationError.unauthorized(
                    "EXPIRED_VERIFICATION_CODE", "The code is no longer valid"));
            case BLOCKED -> Result.failure(ApplicationError.unauthorized(
                    "BLOCKED_VERIFICATION_CODE", "The code ran out of attempts"));
            case VERIFIED -> signInOrAskForRegistration(phoneNumber, command.deviceLabel(), now);
        };
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
