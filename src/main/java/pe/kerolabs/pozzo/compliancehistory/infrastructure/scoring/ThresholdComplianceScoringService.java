package pe.kerolabs.pozzo.compliancehistory.infrastructure.scoring;

import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceLevel;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;
import pe.kerolabs.pozzo.compliancehistory.domain.services.ComplianceScoringService;

import java.util.List;

/**
 * Computes the level with fixed thresholds on the percentage of contributions made on time:
 * NEW without contributions, RISKY after a dropout or below 60 %, REGULAR from 60 %, GOOD from 80 %,
 * and EXCELLENT from 90 % with at least three completed cycles.
 */
@Service
public class ThresholdComplianceScoringService implements ComplianceScoringService {

    static final int EXCELLENT_RATE = 90;
    static final int EXCELLENT_CYCLES = 3;
    static final int GOOD_RATE = 80;
    static final int REGULAR_RATE = 60;

    @Override
    public ComplianceSummary summarize(List<ComplianceEntry> entries) {
        var onTime = count(entries, EntryKind.ON_TIME);
        var late = count(entries, EntryKind.LATE);
        var covered = count(entries, EntryKind.COVERED);
        var rejected = count(entries, EntryKind.REJECTED);
        var dropouts = count(entries, EntryKind.DROPOUT);
        var cycles = count(entries, EntryKind.CYCLE_COMPLETED);
        var counts = new ComplianceSummary(onTime, late, covered, rejected, dropouts, cycles, ComplianceLevel.NEW);
        return new ComplianceSummary(onTime, late, covered, rejected, dropouts, cycles, levelOf(counts));
    }

    private static ComplianceLevel levelOf(ComplianceSummary counts) {
        var rate = counts.complianceRate();
        if (rate == null) {
            return counts.dropouts() > 0 ? ComplianceLevel.RISKY : ComplianceLevel.NEW;
        }
        if (counts.dropouts() > 0 || rate < REGULAR_RATE) {
            return ComplianceLevel.RISKY;
        }
        if (rate >= EXCELLENT_RATE && counts.cyclesCompleted() >= EXCELLENT_CYCLES) {
            return ComplianceLevel.EXCELLENT;
        }
        return rate >= GOOD_RATE ? ComplianceLevel.GOOD : ComplianceLevel.REGULAR;
    }

    private static int count(List<ComplianceEntry> entries, EntryKind kind) {
        return (int) entries.stream().filter(entry -> entry.getKind() == kind).count();
    }
}
