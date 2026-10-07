package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationCode;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities.InvitationPersistenceEntity;

/**
 * Static assembler between the invitation aggregate and its persistence entity.
 */
public final class InvitationPersistenceAssembler {

    private InvitationPersistenceAssembler() {
    }

    public static Invitation toDomainFromPersistence(InvitationPersistenceEntity entity) {
        var invitation = new Invitation();
        invitation.restoreState(
                entity.getId(),
                entity.getGroupId(),
                new InvitationCode(entity.getCode()),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getStatus());
        return invitation;
    }

    /**
     * Copies the state of the aggregate onto a new or already loaded persistence entity.
     */
    public static InvitationPersistenceEntity toPersistenceFromDomain(Invitation invitation,
                                                                      InvitationPersistenceEntity entity) {
        entity.setId(invitation.getId());
        entity.setGroupId(invitation.getGroupId());
        entity.setCode(invitation.getCode().value());
        entity.setCreatedBy(invitation.getCreatedBy());
        entity.setCreatedAt(invitation.getCreatedAt());
        entity.setExpiresAt(invitation.getExpiresAt());
        entity.setStatus(invitation.getStatus());
        return entity;
    }
}
