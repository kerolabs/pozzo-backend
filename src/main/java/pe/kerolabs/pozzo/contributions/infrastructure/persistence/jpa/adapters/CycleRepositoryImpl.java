package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.repositories.CycleRepository;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.assemblers.CyclePersistenceAssembler;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.CyclePersistenceEntity;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.repositories.CyclePersistenceRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the cycle repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class CycleRepositoryImpl implements CycleRepository {

    private final CyclePersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CycleRepositoryImpl(CyclePersistenceRepository persistenceRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Cycle> findById(UUID id) {
        return persistenceRepository.findById(id).map(CyclePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Cycle> findByGroupId(UUID groupId) {
        return persistenceRepository.findByGroupId(groupId).map(CyclePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByGroupId(UUID groupId) {
        return persistenceRepository.existsByGroupId(groupId);
    }

    @Override
    public Cycle save(Cycle cycle) {
        var entity = persistenceRepository.findById(cycle.getId()).orElseGet(CyclePersistenceEntity::new);
        var saved = persistenceRepository.save(CyclePersistenceAssembler.toPersistenceFromDomain(cycle, entity));
        cycle.domainEvents().forEach(eventPublisher::publishEvent);
        cycle.clearDomainEvents();
        return CyclePersistenceAssembler.toDomainFromPersistence(saved);
    }
}
