package pe.kerolabs.pozzo.iam.domain.repositories;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Account repository port.
 */
public interface AccountRepository {

    Optional<Account> findById(UUID id);

    Optional<Account> findByPhoneNumber(PhoneNumber phoneNumber);

    List<Account> findAllByIds(Collection<UUID> ids);

    boolean existsByPhoneNumber(PhoneNumber phoneNumber);

    /**
     * The active account whose profile has this backup email, already in lower case.
     */
    Optional<Account> findActiveByBackupEmail(String email);

    /**
     * True when an account other than {@code accountId} already uses the backup email.
     */
    boolean isBackupEmailUsedByAnother(String email, UUID accountId);

    Account save(Account account);
}
