package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.repositories.AccountRepository;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.assemblers.AccountPersistenceAssembler;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.AccountPersistenceEntity;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.repositories.AccountPersistenceRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the account repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AccountRepositoryImpl(AccountPersistenceRepository persistenceRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return persistenceRepository.findById(id).map(AccountPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Account> findByPhoneNumber(PhoneNumber phoneNumber) {
        return persistenceRepository.findByPhoneNumber(phoneNumber.e164())
                .map(AccountPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
        return persistenceRepository.existsByPhoneNumber(phoneNumber.e164());
    }

    @Override
    public Account save(Account account) {
        var entity = persistenceRepository.findById(account.getId()).orElseGet(AccountPersistenceEntity::new);
        var saved = persistenceRepository.save(AccountPersistenceAssembler.toPersistenceFromDomain(account, entity));
        account.domainEvents().forEach(eventPublisher::publishEvent);
        account.clearDomainEvents();
        return AccountPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
