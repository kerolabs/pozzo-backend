package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.repositories.PeriodRepository;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.assemblers.PeriodPersistenceAssembler;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.PeriodPersistenceEntity;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.repositories.PeriodPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the period repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class PeriodRepositoryImpl implements PeriodRepository {

    private final PeriodPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PeriodRepositoryImpl(PeriodPersistenceRepository persistenceRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Period> findById(UUID id) {
        return persistenceRepository.findById(id).map(PeriodPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Period> findByCycleIdAndTurnNumber(UUID cycleId, int turnNumber) {
        return persistenceRepository.findByCycleIdAndTurnNumber(cycleId, turnNumber)
                .map(PeriodPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Period> findAllByCycleId(UUID cycleId) {
        return persistenceRepository.findAllByCycleIdOrderByTurnNumberAsc(cycleId).stream()
                .map(PeriodPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Period save(Period period) {
        var entity = persistenceRepository.findById(period.getId()).orElseGet(PeriodPersistenceEntity::new);
        var saved = persistenceRepository.save(PeriodPersistenceAssembler.toPersistenceFromDomain(period, entity));
        period.domainEvents().forEach(eventPublisher::publishEvent);
        period.clearDomainEvents();
        return PeriodPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
