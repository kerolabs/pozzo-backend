package pe.kerolabs.pozzo.iam.domain.model.valueobjects;

/**
 * Result of one attempt to verify a code.
 */
public enum VerificationOutcome {
    /** The code matched and was still valid. */
    VERIFIED,
    /** The code did not match; attempts remain. */
    INVALID,
    /** The code was no longer valid. */
    EXPIRED,
    /** The code ran out of attempts. */
    BLOCKED
}
