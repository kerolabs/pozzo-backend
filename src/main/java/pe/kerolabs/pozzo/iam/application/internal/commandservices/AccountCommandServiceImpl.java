package pe.kerolabs.pozzo.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.iam.application.commandservices.AccountCommandService;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.photos.ProfilePhotoStorage;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.commands.ChangePhoneNumberCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.ChangeProfilePhotoCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestPhoneChangeCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.UpdateProfileCommand;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Profile;
import pe.kerolabs.pozzo.iam.domain.repositories.AccountRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;
import java.util.Set;
import java.util.UUID;

/**
 * Account command service implementation: the profile and the phone number of a signed-in member.
 */
@Service
@Transactional
public class AccountCommandServiceImpl implements AccountCommandService {

    /** Photos larger than this are rejected; the app sends them already reduced. */
    static final int MAX_PHOTO_BYTES = 2 * 1024 * 1024;
    private static final Set<String> PHOTO_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final AccountRepository accountRepository;
    private final PhoneCodes phoneCodes;
    private final ProfilePhotoStorage photoStorage;
    private final Clock clock;

    public AccountCommandServiceImpl(AccountRepository accountRepository, PhoneCodes phoneCodes,
                                     ProfilePhotoStorage photoStorage, Clock clock) {
        this.accountRepository = accountRepository;
        this.phoneCodes = phoneCodes;
        this.photoStorage = photoStorage;
        this.clock = clock;
    }

    @Override
    public Result<Account, ApplicationError> handle(UpdateProfileCommand command) {
        return findAccount(command.accountId()).flatMap(account -> {
            var walletNumber = command.walletNumber() == null || command.walletNumber().isBlank()
                    ? null : PhoneNumber.ofPeruvianMobile(command.walletNumber());
            var profile = new Profile(command.displayName(), command.photoUrl(), command.theme(),
                    walletNumber, command.backupEmail());
            // The backup email identifies the account to recover, so two accounts cannot share it.
            if (profile.backupEmail() != null
                    && accountRepository.isBackupEmailUsedByAnother(profile.backupEmail(), account.getId())) {
                return Result.failure(ApplicationError.businessRuleViolation(
                        "BACKUP_EMAIL_IN_USE", "The backup email belongs to another account"));
            }
            account.updateProfile(profile, clock.instant());
            return Result.success(accountRepository.save(account));
        });
    }

    @Override
    public Result<Account, ApplicationError> handle(ChangeProfilePhotoCommand command) {
        var content = command.content();
        var contentType = command.contentType();
        if (content != null && (contentType == null || !PHOTO_TYPES.contains(contentType))) {
            return Result.failure(ApplicationError.validationError("photo", "must be a JPEG, PNG or WebP image"));
        }
        if (content != null && (content.length == 0 || content.length > MAX_PHOTO_BYTES)) {
            return Result.failure(ApplicationError.validationError("photo", "must weigh up to 2 MB"));
        }
        return findAccount(command.accountId()).map(account -> {
            var previous = account.getProfile().photoUrl();
            var photoUrl = content == null ? null : photoStorage.store(account.getId(), content, contentType);
            var profile = account.getProfile();
            account.updateProfile(new Profile(profile.displayName(), photoUrl, profile.theme(),
                    profile.walletNumber(), profile.backupEmail()), clock.instant());
            var saved = accountRepository.save(account);
            if (previous != null) {
                photoStorage.delete(previous);
            }
            return saved;
        });
    }

    @Override
    public Result<VerificationCode, ApplicationError> handle(RequestPhoneChangeCodeCommand command) {
        var now = clock.instant();
        return findAccount(command.accountId())
                .flatMap(account -> requireNewAndFree(account, command.phoneNumber()))
                .flatMap(phoneNumber -> phoneCodes.issue(phoneNumber, now));
    }

    @Override
    public Result<Account, ApplicationError> handle(ChangePhoneNumberCommand command) {
        var now = clock.instant();
        return findAccount(command.accountId()).flatMap(account ->
                requireNewAndFree(account, command.phoneNumber())
                        .flatMap(phoneNumber -> phoneCodes.verify(phoneNumber, command.code(), now))
                        .map(phoneNumber -> {
                            account.changePhoneNumber(phoneNumber, false, now);
                            return accountRepository.save(account);
                        }));
    }

    private Result<Account, ApplicationError> findAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .<Result<Account, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(ApplicationError.notFound("Account", accountId.toString())));
    }

    private Result<PhoneNumber, ApplicationError> requireNewAndFree(Account account, PhoneNumber phoneNumber) {
        if (account.getPhoneNumber().equals(phoneNumber)) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "PHONE_NUMBER_UNCHANGED", "The account already uses this phone number"));
        }
        if (accountRepository.existsByPhoneNumber(phoneNumber)) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "PHONE_NUMBER_IN_USE", "The phone number belongs to another account"));
        }
        return Result.success(phoneNumber);
    }
}
