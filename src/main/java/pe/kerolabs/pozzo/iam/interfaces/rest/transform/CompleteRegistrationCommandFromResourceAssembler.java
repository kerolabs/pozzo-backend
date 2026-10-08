package pe.kerolabs.pozzo.iam.interfaces.rest.transform;

import pe.kerolabs.pozzo.iam.domain.model.commands.CompleteRegistrationCommand;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.RegisterResource;

/**
 * Translates {@link RegisterResource} into {@link CompleteRegistrationCommand}.
 */
public class CompleteRegistrationCommandFromResourceAssembler {

    public static CompleteRegistrationCommand toCommandFromResource(RegisterResource resource) {
        return new CompleteRegistrationCommand(
                resource.registrationToken(),
                resource.displayName(),
                resource.photoUrl(),
                resource.termsAccepted(),
                resource.deviceLabel());
    }
}
