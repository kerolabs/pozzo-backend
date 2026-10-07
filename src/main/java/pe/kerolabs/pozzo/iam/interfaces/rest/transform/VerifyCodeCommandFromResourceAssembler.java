package pe.kerolabs.pozzo.iam.interfaces.rest.transform;

import pe.kerolabs.pozzo.iam.domain.model.commands.VerifyCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.VerifyCodeResource;

/**
 * Translates {@link VerifyCodeResource} into {@link VerifyCodeCommand}.
 */
public class VerifyCodeCommandFromResourceAssembler {

    public static VerifyCodeCommand toCommandFromResource(VerifyCodeResource resource) {
        return new VerifyCodeCommand(
                PhoneNumber.ofPeruvianMobile(resource.phoneNumber()), resource.code(), resource.deviceLabel());
    }
}
