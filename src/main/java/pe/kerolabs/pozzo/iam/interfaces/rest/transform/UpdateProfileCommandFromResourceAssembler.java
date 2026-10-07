package pe.kerolabs.pozzo.iam.interfaces.rest.transform;

import pe.kerolabs.pozzo.iam.domain.model.commands.UpdateProfileCommand;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.UpdateProfileResource;

import java.util.UUID;

/**
 * Translates {@link UpdateProfileResource}, plus the authenticated account, into {@link UpdateProfileCommand}.
 */
public class UpdateProfileCommandFromResourceAssembler {

    public static UpdateProfileCommand toCommandFromResource(UUID accountId, UpdateProfileResource resource) {
        return new UpdateProfileCommand(accountId, resource.displayName(), resource.photoUrl(), resource.theme());
    }
}
