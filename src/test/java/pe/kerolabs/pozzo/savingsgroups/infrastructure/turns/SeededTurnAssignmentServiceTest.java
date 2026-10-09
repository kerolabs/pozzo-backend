package pe.kerolabs.pozzo.savingsgroups.infrastructure.turns;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupRules;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Periodicity;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnSlot;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SeededTurnAssignmentService: a draw any member can reproduce")
class SeededTurnAssignmentServiceTest {

    private final SeededTurnAssignmentService service = new SeededTurnAssignmentService();
    private SavingsGroup group;

    @BeforeEach
    void fullGroupOfSix() {
        var now = Instant.parse("2026-10-08T15:00:00Z");
        group = SavingsGroup.create(UUID.randomUUID(), "Anna Weber", "Junta de la familia Weber",
                new GroupRules(Money.soles(new BigDecimal("200")), Periodicity.MONTHLY, 6, LocalDate.parse("2026-10-20"), null),
                now);
        for (var name : new String[]{"Sofia Gonzales", "Marta Quispe", "Jorge Ramos", "Carla Vega", "Luis Paz"}) {
            group.join(UUID.randomUUID(), name, now);
        }
    }

    @Test
    void givesEveryMemberOneTurnFromOneToTheNumberOfMembers() {
        var turns = service.drawTurns(group, "cad8b2010935012d");

        assertThat(turns).extracting(TurnSlot::turnNumber).containsExactly(1, 2, 3, 4, 5, 6);
        assertThat(turns).extracting(TurnSlot::membershipId).doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(group.activeMemberships().stream().map(Membership::getId).toList());
        assertThat(turns).allMatch(turn -> turn.assignedBy() == TurnMethod.DRAW);
    }

    @Test
    void theSameSeedAlwaysGivesTheSameOrder() {
        assertThat(service.drawTurns(group, "cad8b2010935012d")).isEqualTo(service.drawTurns(group, "cad8b2010935012d"));
    }

    @Test
    void anotherSeedGivesAnotherOrder() {
        assertThat(service.drawTurns(group, "cad8b2010935012d")).isNotEqualTo(service.drawTurns(group, "5bd6904907be3299"));
    }

    @Test
    void theAgreedOrderKeepsTheOrderTheOrganizerSent() {
        var order = group.activeMemberships().reversed().stream().map(Membership::getId).toList();

        var turns = service.agreedTurns(group, order);

        assertThat(turns).extracting(TurnSlot::membershipId).containsExactlyElementsOf(order);
        assertThat(turns.getFirst().assignedBy()).isEqualTo(TurnMethod.AGREED);
    }
}
