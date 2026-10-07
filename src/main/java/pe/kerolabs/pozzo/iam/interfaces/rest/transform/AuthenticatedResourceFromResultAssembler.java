package pe.kerolabs.pozzo.iam.interfaces.rest.transform;

import pe.kerolabs.pozzo.iam.application.commandservices.AuthenticatedAccount;
import pe.kerolabs.pozzo.iam.application.commandservices.CodeVerification;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.AuthenticatedResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.VerificationResource;

/**
 * Converts the results of the access use cases into their REST resources.
 */
public class AuthenticatedResourceFromResultAssembler {

    public static AuthenticatedResource toResourceFromResult(AuthenticatedAccount authenticated) {
        return new AuthenticatedResource(
                authenticated.token(),
                authenticated.expiresAt(),
                ProfileResourceFromEntityAssembler.toResourceFromEntity(authenticated.account()));
    }

    public static VerificationResource toResourceFromResult(CodeVerification verification) {
        if (verification.isRegistrationRequired()) {
            return new VerificationResource(true, null,
                    verification.registrationToken(), verification.registrationTokenExpiresAt());
        }
        return new VerificationResource(false, toResourceFromResult(verification.authenticated()), null, null);
    }
}
