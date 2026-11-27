package pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.GroupCompliance;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MemberRecord: the compliance history of a member")
class MemberRecordTest {

    private static final Instant MONDAY = Instant.parse("2026-11-02T15:00:00Z");
    private static final UUID FAMILY = UUID.randomUUID();
    private static final UUID OFFICE = UUID.randomUUID();

    @Test
    void groupsTheContributionsOfEachGroupByKind() {
        var record = recordOf(
                entry(FAMILY, "Junta familiar", EntryKind.ON_TIME, 0),
                entry(FAMILY, "Junta familiar", EntryKind.LATE, 30),
                entry(FAMILY, "Junta familiar", EntryKind.COVERED, 60),
                entry(FAMILY, "Junta familiar", EntryKind.REJECTED, 61),
                entry(OFFICE, "Junta de la oficina", EntryKind.ON_TIME, 10));

        var family = groupOf(record, FAMILY);

        assertThat(family.groupName()).isEqualTo("Junta familiar");
        assertThat(family.onTime()).isEqualTo(1);
        assertThat(family.late()).isEqualTo(1);
        assertThat(family.covered()).isEqualTo(1);
        assertThat(family.contributions()).isEqualTo(3);
        assertThat(groupOf(record, OFFICE).contributions()).isEqualTo(1);
    }

    @Test
    void aGroupIsCompletedOnceTheMemberFinishedItsCycle() {
        var record = recordOf(
                entry(FAMILY, "Junta familiar", EntryKind.ON_TIME, 0),
                entry(FAMILY, "Junta familiar", EntryKind.CYCLE_COMPLETED, 90),
                entry(OFFICE, "Junta de la oficina", EntryKind.ON_TIME, 10));

        assertThat(groupOf(record, FAMILY).completed()).isTrue();
        assertThat(groupOf(record, OFFICE).completed()).isFalse();
    }

    @Test
    void theFirstAndLastActivityOrderTheGroups() {
        var record = recordOf(
                entry(OFFICE, "Junta de la oficina", EntryKind.ON_TIME, 40),
                entry(FAMILY, "Junta familiar", EntryKind.ON_TIME, 0),
                entry(FAMILY, "Junta familiar", EntryKind.ON_TIME, 70));

        var byStart = record.groups().stream().sorted(Comparator.comparing(GroupCompliance::firstActivity)).toList();

        assertThat(byStart).extracting(GroupCompliance::groupId).containsExactly(FAMILY, OFFICE);
        assertThat(groupOf(record, FAMILY).lastActivity()).isEqualTo(MONDAY.plusSeconds(70 * 86_400L));
    }

    private static MemberRecord recordOf(ComplianceEntry... entries) {
        var record = new MemberRecord();
        record.restoreState(UUID.randomUUID(), List.of(entries), ComplianceSummary.empty(), MONDAY);
        return record;
    }

    private static ComplianceEntry entry(UUID groupId, String groupName, EntryKind kind, int day) {
        return ComplianceEntry.of(UUID.randomUUID(), groupId, groupName, null, kind,
                MONDAY.plusSeconds(day * 86_400L), UUID.randomUUID().toString());
    }

    private static GroupCompliance groupOf(MemberRecord record, UUID groupId) {
        return record.groups().stream().filter(group -> group.groupId().equals(groupId)).findFirst().orElseThrow();
    }
}
