package pe.kerolabs.pozzo.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.iam.application.commandservices.AccountCommandService;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.commands.UpdateProfileCommand;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Profile;
import pe.kerolabs.pozzo.iam.domain.repositories.AccountRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;

/**
 * Account command service implementation.
 */
@Service
@Transactional
public class AccountCommandServiceImpl implements AccountCommandService {

    private final AccountRepository accountRepository;
    private final Clock clock;

    public AccountCommandServiceImpl(AccountRepository accountRepository, Clock clock) {
        this.accountRepository = accountRepository;
        this.clock = clock;
    }

    @Override
    public Result<Account, ApplicationError> handle(UpdateProfileCommand command) {
        return accountRepository.findById(command.accountId())
                .<Result<Account, ApplicationError>>map(account -> {
                    var walletNumber = command.walletNumber() == null || command.walletNumber().isBlank()
                            ? null : PhoneNumber.ofPeruvianMobile(command.walletNumber());
                    account.updateProfile(
                            new Profile(command.displayName(), command.photoUrl(), command.theme(),
                                    walletNumber, command.backupEmail()),
                            clock.instant());
                    return Result.success(accountRepository.save(account));
                })
                .orElseGet(() -> Result.failure(ApplicationError.notFound("Account", command.accountId().toString())));
    }
}
