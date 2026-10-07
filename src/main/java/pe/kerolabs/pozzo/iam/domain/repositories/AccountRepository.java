package pe.kerolabs.pozzo.iam.domain.repositories;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

import java.util.Optional;
import java.util.UUID;

/**
 * Account repository port.
 */
public interface AccountRepository {

    Optional<Account> findById(UUID id);

    Optional<Account> findByPhoneNumber(PhoneNumber phoneNumber);

    boolean existsByPhoneNumber(PhoneNumber phoneNumber);

    Account save(Account account);
}
