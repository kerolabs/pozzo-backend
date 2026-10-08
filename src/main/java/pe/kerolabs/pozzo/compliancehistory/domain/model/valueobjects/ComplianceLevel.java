package pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects;

/**
 * Overall level of a member's compliance. NEW until the member has contributions on record.
 */
public enum ComplianceLevel {
    EXCELLENT,
    GOOD,
    REGULAR,
    RISKY,
    NEW
}
