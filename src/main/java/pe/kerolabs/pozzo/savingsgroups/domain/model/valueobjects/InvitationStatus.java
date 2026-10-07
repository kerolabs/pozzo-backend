package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

/**
 * State of an invitation. It expires when a new one replaces it, when it reaches its date
 * or when the group starts.
 */
public enum InvitationStatus {
    ACTIVE,
    EXPIRED
}
