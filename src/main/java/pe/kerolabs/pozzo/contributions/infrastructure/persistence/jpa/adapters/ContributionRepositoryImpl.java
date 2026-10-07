package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.repositories.ContributionRepository;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.assemblers.ContributionPersistenceAssembler;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.ContributionPersistenceEntity;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.repositories.ContributionPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the contribution repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class ContributionRepositoryImpl implements ContributionRepository {

    private final ContributionPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ContributionRepositoryImpl(ContributionPersistenceRepository persistenceRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Contribution> findById(UUID id) {
        return persistenceRepository.findById(id).map(ContributionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Contribution> findAllByPeriodId(UUID periodId) {
        return persistenceRepository.findAllByPeriodIdOrderByRegisteredAtDesc(periodId).stream()
                .map(ContributionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Contribution> findAllByCycleIdAndMembershipId(UUID cycleId, UUID membershipId) {
        return persistenceRepository.findAllByCycleIdAndMembershipIdOrderByRegisteredAtDesc(cycleId, membershipId)
                .stream()
                .map(ContributionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByCycleIdAndOperationNumber(UUID cycleId, String operationNumber) {
        return persistenceRepository.existsByCycleIdAndReceiptOperationNumber(cycleId, operationNumber);
    }

    @Override
    public Contribution save(Contribution contribution) {
        var entity = persistenceRepository.findById(contribution.getId())
                .orElseGet(ContributionPersistenceEntity::new);
        var saved = persistenceRepository.save(
                ContributionPersistenceAssembler.toPersistenceFromDomain(contribution, entity));
        contribution.domainEvents().forEach(eventPublisher::publishEvent);
        contribution.clearDomainEvents();
        return ContributionPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
