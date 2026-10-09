package pe.kerolabs.pozzo.compliancehistory.infrastructure.scoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceLevel;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ThresholdComplianceScoringService: the level of a member from their history")
class ThresholdComplianceScoringServiceTest {

    private final ThresholdComplianceScoringService scoring = new ThresholdComplianceScoringService();

    private static List<ComplianceEntry> entries(int onTime, int late, int covered, int dropouts, int cycles) {
        var entries = new ArrayList<ComplianceEntry>();
        add(entries, EntryKind.ON_TIME, onTime);
        add(entries, EntryKind.LATE, late);
        add(entries, EntryKind.COVERED, covered);
        add(entries, EntryKind.DROPOUT, dropouts);
        add(entries, EntryKind.CYCLE_COMPLETED, cycles);
        return entries;
    }

    private static void add(List<ComplianceEntry> entries, EntryKind kind, int times) {
        for (int i = 0; i < times; i++) {
            entries.add(ComplianceEntry.of(UUID.randomUUID(), UUID.randomUUID(), "Junta de la familia Weber", null,
                    kind, Instant.parse("2026-10-08T15:00:00Z"), UUID.randomUUID().toString()));
        }
    }

    @Test
    void aMemberWithoutContributionsIsNew() {
        var summary = scoring.summarize(List.of());

        assertThat(summary.level()).isEqualTo(ComplianceLevel.NEW);
        assertThat(summary.complianceRate()).isNull();
    }

    @Test
    void countsLateAndCoveredContributionsAgainstTheRate() {
        var summary = scoring.summarize(entries(8, 1, 1, 0, 0));

        assertThat(summary.contributions()).isEqualTo(10);
        assertThat(summary.complianceRate()).isEqualTo(80);
        assertThat(summary.level()).isEqualTo(ComplianceLevel.GOOD);
    }

    @Test
    void excellentNeedsNinetyPercentAndThreeCompletedCycles() {
        assertThat(scoring.summarize(entries(10, 0, 0, 0, 2)).level()).isEqualTo(ComplianceLevel.GOOD);
        assertThat(scoring.summarize(entries(10, 0, 0, 0, 3)).level()).isEqualTo(ComplianceLevel.EXCELLENT);
    }

    @Test
    void belowSixtyPercentIsRiskyAndFromSixtyIsRegular() {
        assertThat(scoring.summarize(entries(5, 5, 0, 0, 0)).level()).isEqualTo(ComplianceLevel.RISKY);
        assertThat(scoring.summarize(entries(7, 3, 0, 0, 0)).level()).isEqualTo(ComplianceLevel.REGULAR);
    }

    @Test
    void aDropoutMakesTheMemberRiskyWhateverTheRate() {
        assertThat(scoring.summarize(entries(10, 0, 0, 1, 3)).level()).isEqualTo(ComplianceLevel.RISKY);
        assertThat(scoring.summarize(entries(0, 0, 0, 1, 0)).level()).isEqualTo(ComplianceLevel.RISKY);
    }
}
