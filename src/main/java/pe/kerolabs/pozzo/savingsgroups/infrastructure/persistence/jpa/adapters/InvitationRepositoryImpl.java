package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationCode;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationStatus;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.InvitationRepository;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.assemblers.InvitationPersistenceAssembler;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities.InvitationPersistenceEntity;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.repositories.InvitationPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the invitation repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class InvitationRepositoryImpl implements InvitationRepository {

    private final InvitationPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InvitationRepositoryImpl(InvitationPersistenceRepository persistenceRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Invitation> findByCode(InvitationCode code) {
        return persistenceRepository.findByCode(code.value())
                .map(InvitationPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByCode(InvitationCode code) {
        return persistenceRepository.existsByCode(code.value());
    }

    @Override
    public List<Invitation> findAllActiveByGroupId(UUID groupId) {
        return persistenceRepository.findAllByGroupIdAndStatus(groupId, InvitationStatus.ACTIVE).stream()
                .map(InvitationPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Invitation save(Invitation invitation) {
        var entity = persistenceRepository.findById(invitation.getId()).orElseGet(InvitationPersistenceEntity::new);
        var saved = persistenceRepository.save(
                InvitationPersistenceAssembler.toPersistenceFromDomain(invitation, entity));
        invitation.domainEvents().forEach(eventPublisher::publishEvent);
        invitation.clearDomainEvents();
        return InvitationPersistenceAssembler.toDomainFromPersistence(saved);
    }
}
