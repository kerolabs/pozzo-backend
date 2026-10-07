package pe.kerolabs.pozzo.iam.application.commandservices;

import java.time.Instant;

/**
 * The member proved the account with the backup email.
 *
 * @param recoveryToken the token that allows linking a new number to the account
 * @param expiresAt     the moment the token expires
 */
public record RecoveryVerification(String recoveryToken, Instant expiresAt) {
}
