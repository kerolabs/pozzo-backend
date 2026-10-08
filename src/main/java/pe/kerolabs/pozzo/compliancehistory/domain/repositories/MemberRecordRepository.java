package pe.kerolabs.pozzo.compliancehistory.domain.repositories;

import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;

import java.util.Optional;
import java.util.UUID;

/**
 * Member record repository port.
 */
public interface MemberRecordRepository {

    /**
     * Finds the record of a member; empty when no fact was recorded for the account yet.
     */
    Optional<MemberRecord> findByAccountId(UUID accountId);

    /**
     * Whether a fact produced by this event was already recorded for any member.
     */
    boolean existsEntryBySourceEventId(String sourceEventId);

    /**
     * Creates or updates the record with its entries and returns the stored state.
     */
    MemberRecord save(MemberRecord record);
}
