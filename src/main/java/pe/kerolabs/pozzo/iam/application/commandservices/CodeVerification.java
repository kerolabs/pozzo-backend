package pe.kerolabs.pozzo.iam.application.commandservices;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * What happens after a correct code: a registered member signs in, while a new member
 * receives a registration token to complete the registration.
 *
 * @param authenticated                the opened session, when the phone number already had an account
 * @param registrationToken            the token that proves the number, when it had no account
 * @param registrationTokenExpiresAt   the moment the registration token expires
 */
public record CodeVerification(@Nullable AuthenticatedAccount authenticated,
                               @Nullable String registrationToken,
                               @Nullable Instant registrationTokenExpiresAt) {

    public static CodeVerification signedIn(AuthenticatedAccount authenticated) {
        return new CodeVerification(authenticated, null, null);
    }

    public static CodeVerification registrationRequired(String registrationToken, Instant expiresAt) {
        return new CodeVerification(null, registrationToken, expiresAt);
    }

    public boolean isRegistrationRequired() {
        return authenticated == null;
    }
}
