package pe.kerolabs.pozzo.iam.domain.model.valueobjects;

/**
 * Lifecycle state of a verification code.
 * A code is PENDING until it is verified, expires, is replaced by a newer code or runs out of attempts.
 */
public enum VerificationStatus {
    PENDING,
    VERIFIED,
    EXPIRED,
    BLOCKED
}
