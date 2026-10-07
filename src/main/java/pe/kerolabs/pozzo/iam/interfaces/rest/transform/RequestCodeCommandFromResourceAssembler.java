package pe.kerolabs.pozzo.iam.interfaces.rest.transform;

import pe.kerolabs.pozzo.iam.domain.model.commands.RequestCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.RequestCodeResource;

/**
 * Translates {@link RequestCodeResource} into {@link RequestCodeCommand}.
 */
public class RequestCodeCommandFromResourceAssembler {

    public static RequestCodeCommand toCommandFromResource(RequestCodeResource resource) {
        return new RequestCodeCommand(PhoneNumber.ofPeruvianMobile(resource.phoneNumber()));
    }
}
