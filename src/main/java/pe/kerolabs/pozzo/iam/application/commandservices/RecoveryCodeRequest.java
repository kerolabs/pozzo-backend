package pe.kerolabs.pozzo.iam.application.commandservices;

import java.time.Instant;

/**
 * A recovery code was requested. The same answer is given whether the email belongs to an account or not.
 *
 * @param email             the email, in lower case
 * @param expiresAt         the moment the code stops being valid
 * @param resendAvailableAt the moment from which a new code may be requested
 */
public record RecoveryCodeRequest(String email, Instant expiresAt, Instant resendAvailableAt) {
}
