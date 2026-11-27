package pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects;

import java.time.Instant;
import java.util.UUID;

/**
 * Compliance of a member in one group: their contributions by kind and whether they took part until the
 * cycle closed. The first and last activity order the groups.
 */
public record GroupCompliance(UUID groupId, String groupName, boolean completed, int onTime, int late, int covered,
                              Instant firstActivity, Instant lastActivity) {

    /**
     * Contributions on record in the group: on time, late and covered.
     */
    public int contributions() {
        return onTime + late + covered;
    }
}
