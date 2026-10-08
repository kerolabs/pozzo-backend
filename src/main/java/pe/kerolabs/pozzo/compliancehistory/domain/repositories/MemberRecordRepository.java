package pe.kerolabs.pozzo.compliancehistory.domain.repositories;

import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;

import java.util.Optional;
import java.util.UUID;

/**
 * Member record repository port.
 */
public interface MemberRecordRepository {

    Optional<MemberRecord> findByAccountId(UUID accountId);

    /**
     * Whether a fact produced by this event was already recorded for any member.
     */
    boolean existsEntryBySourceEventId(String sourceEventId);

    MemberRecord save(MemberRecord record);
}
