package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipStatus;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.SavingsGroupRepository;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.assemblers.SavingsGroupPersistenceAssembler;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities.SavingsGroupPersistenceEntity;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.repositories.SavingsGroupPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the savings group repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class SavingsGroupRepositoryImpl implements SavingsGroupRepository {

    private static final List<MembershipStatus> ACTIVE_STATUSES =
            List.of(MembershipStatus.ACTIVE, MembershipStatus.REPLACEMENT);

    private final SavingsGroupPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SavingsGroupRepositoryImpl(SavingsGroupPersistenceRepository persistenceRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<SavingsGroup> findById(UUID id) {
        return persistenceRepository.findById(id).map(SavingsGroupPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<SavingsGroup> findAllByMemberId(UUID memberId) {
        return persistenceRepository.findAllByMember(memberId, ACTIVE_STATUSES).stream()
                .map(SavingsGroupPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public SavingsGroup save(SavingsGroup group) {
        var entity = persistenceRepository.findById(group.getId()).orElseGet(SavingsGroupPersistenceEntity::new);
        var saved = persistenceRepository.save(SavingsGroupPersistenceAssembler.toPersistenceFromDomain(group, entity));
        group.domainEvents().forEach(eventPublisher::publishEvent);
        group.clearDomainEvents();
        return SavingsGroupPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override
    public void delete(SavingsGroup group) {
        persistenceRepository.deleteById(group.getId());
        group.domainEvents().forEach(eventPublisher::publishEvent);
        group.clearDomainEvents();
    }
}
