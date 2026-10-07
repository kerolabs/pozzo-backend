package pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects;

/**
 * Kind of compliance fact recorded for a member.
 */
public enum EntryKind {
    /** A contribution settled by its cutoff date. */
    ON_TIME,
    /** A contribution settled after its cutoff date. */
    LATE,
    /** Another member put the money of a contribution. */
    COVERED,
    /** The organizer rejected a receipt. */
    REJECTED,
    /** The member left a started group. */
    DROPOUT,
    /** The member took part in a cycle until its end. */
    CYCLE_COMPLETED;

    /**
     * Returns true for the facts that count as a contribution in the compliance rate.
     */
    public boolean isContribution() {
        return this == ON_TIME || this == LATE || this == COVERED;
    }
}
