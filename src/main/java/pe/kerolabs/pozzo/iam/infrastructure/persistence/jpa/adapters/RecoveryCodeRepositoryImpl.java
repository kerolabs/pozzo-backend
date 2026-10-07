package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.RecoveryCode;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationStatus;
import pe.kerolabs.pozzo.iam.domain.repositories.RecoveryCodeRepository;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.assemblers.RecoveryCodePersistenceAssembler;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.RecoveryCodePersistenceEntity;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.repositories.RecoveryCodePersistenceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements the recovery code repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class RecoveryCodeRepositoryImpl implements RecoveryCodeRepository {

    private final RecoveryCodePersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RecoveryCodeRepositoryImpl(RecoveryCodePersistenceRepository persistenceRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<RecoveryCode> findLatestByEmail(String email) {
        return persistenceRepository.findFirstByEmailOrderByIssuedAtDesc(email)
                .map(RecoveryCodePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<RecoveryCode> findPendingByEmail(String email) {
        return persistenceRepository.findFirstByEmailAndStatusOrderByIssuedAtDesc(email, VerificationStatus.PENDING)
                .map(RecoveryCodePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<RecoveryCode> findAllPendingByEmail(String email) {
        return persistenceRepository.findAllByEmailAndStatus(email, VerificationStatus.PENDING).stream()
                .map(RecoveryCodePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public RecoveryCode save(RecoveryCode recoveryCode) {
        var entity = persistenceRepository.findById(recoveryCode.getId())
                .orElseGet(RecoveryCodePersistenceEntity::new);
        var saved = persistenceRepository.save(
                RecoveryCodePersistenceAssembler.toPersistenceFromDomain(recoveryCode, entity));
        recoveryCode.domainEvents().forEach(eventPublisher::publishEvent);
        recoveryCode.clearDomainEvents();
        return RecoveryCodePersistenceAssembler.toDomainFromPersistence(saved);
    }
}
