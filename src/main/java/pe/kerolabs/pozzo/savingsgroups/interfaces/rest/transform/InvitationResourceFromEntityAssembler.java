package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.InvitationResource;

/**
 * Converts an {@link Invitation} into {@link InvitationResource}.
 */
public class InvitationResourceFromEntityAssembler {

    public static InvitationResource toResourceFromEntity(Invitation invitation, String link) {
        return new InvitationResource(invitation.getCode().value(), link, invitation.getExpiresAt());
    }
}
