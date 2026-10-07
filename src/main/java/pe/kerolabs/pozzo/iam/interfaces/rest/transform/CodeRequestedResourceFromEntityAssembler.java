package pe.kerolabs.pozzo.iam.interfaces.rest.transform;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.CodeRequestedResource;

/**
 * Converts the {@link VerificationCode} aggregate into {@link CodeRequestedResource}.
 */
public class CodeRequestedResourceFromEntityAssembler {

    public static CodeRequestedResource toResourceFromEntity(VerificationCode code) {
        return new CodeRequestedResource(code.getPhoneNumber().e164(), code.getExpiresAt(), code.resendAvailableAt());
    }
}
