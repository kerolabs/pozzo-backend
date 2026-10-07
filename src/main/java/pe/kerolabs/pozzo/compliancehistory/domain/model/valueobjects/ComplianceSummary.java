package pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects;

import org.jspecify.annotations.Nullable;

/**
 * Aggregated compliance of a member: counts by kind, completed cycles and level. It is the only thing
 * shown to an organizer or through a shared link, never amounts or the names of other groups.
 */
public record ComplianceSummary(int onTime, int late, int covered, int rejected, int dropouts, int cyclesCompleted,
                                ComplianceLevel level) {

    public static ComplianceSummary empty() {
        return new ComplianceSummary(0, 0, 0, 0, 0, 0, ComplianceLevel.NEW);
    }

    /**
     * Contributions on record: on time, late and covered.
     */
    public int contributions() {
        return onTime + late + covered;
    }

    /**
     * Percentage of contributions made on time, from 0 to 100; null when there are none yet.
     */
    public @Nullable Integer complianceRate() {
        var total = contributions();
        return total == 0 ? null : (int) Math.round(onTime * 100.0 / total);
    }
}
