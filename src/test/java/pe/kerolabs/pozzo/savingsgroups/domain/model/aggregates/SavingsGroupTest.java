package pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.GroupFilledEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.GroupStartedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.MemberJoinedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.RulesUpdatedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Destination;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupRules;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupStatus;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipKind;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.PaymentMethod;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Periodicity;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnSlot;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SavingsGroup: setup of a savings group until it starts")
class SavingsGroupTest {

    private static final Instant NOW = Instant.parse("2026-10-08T15:00:00Z");

    private final UUID anna = UUID.randomUUID();
    private SavingsGroup group;

    static GroupRules rules(int seats, Destination destination) {
        return new GroupRules(Money.soles(new BigDecimal("200")), Periodicity.MONTHLY, seats,
                LocalDate.parse("2026-10-20"), destination);
    }

    static Destination yape() {
        return Destination.of(PaymentMethod.YAPE, "987654321");
    }

    @BeforeEach
    void createGroupOfThree() {
        group = SavingsGroup.create(anna, "Anna Weber", "Junta de la familia Weber", rules(3, null), NOW);
    }

    private void fill() {
        group.join(UUID.randomUUID(), "Sofia Gonzales", NOW);
        group.join(UUID.randomUUID(), "Jorge Ramos", NOW);
    }

    private void drawTurns() {
        var slots = new ArrayList<TurnSlot>();
        var members = group.activeMemberships();
        for (int i = 0; i < members.size(); i++) {
            slots.add(new TurnSlot(i + 1, members.get(i).getId(), TurnMethod.DRAW));
        }
        group.assignTurns(slots, TurnMethod.DRAW, "seed", NOW);
    }

    @Test
    void startsAsADraftWithItsCreatorAsOrganizerAndFirstMember() {
        assertThat(group.getStatus()).isEqualTo(GroupStatus.DRAFT);
        assertThat(group.isOrganizer(anna)).isTrue();
        assertThat(group.activeMembersCount()).isEqualTo(1);
        assertThat(group.freeSeats()).isEqualTo(2);
    }

    @Test
    void announcesWhenTheLastSeatIsTaken() {
        group.clearDomainEvents();

        fill();

        assertThat(group.isFull()).isTrue();
        assertThat(group.domainEvents()).hasAtLeastOneElementOfType(MemberJoinedEvent.class)
                .hasAtLeastOneElementOfType(GroupFilledEvent.class);
    }

    @Test
    void rejectsAJoinWhenThereIsNoFreeSeat() {
        fill();

        assertThatThrownBy(() -> group.join(UUID.randomUUID(), "Marta Quispe", NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("SAVINGS_GROUP_FULL");
    }

    @Test
    void aMemberCannotJoinTwice() {
        var sofia = UUID.randomUUID();
        group.join(sofia, "Sofia Gonzales", NOW);

        assertThatThrownBy(() -> group.join(sofia, "Sofia Gonzales", NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("ALREADY_A_MEMBER");
    }

    @Test
    void aRemovedMemberCanJoinAgainWithTheSameMembership() {
        var sofia = UUID.randomUUID();
        var membership = group.join(sofia, "Sofia Gonzales", NOW);

        group.removeMember(membership.getId(), NOW);
        var again = group.join(sofia, "Sofia Gonzales", NOW);

        assertThat(again.getId()).isEqualTo(membership.getId());
        assertThat(again.isActive()).isTrue();
    }

    @Test
    void theOrganizerCannotBeRemoved() {
        var organizerMembership = group.membershipOf(anna).orElseThrow();

        assertThatThrownBy(() -> group.removeMember(organizerMembership.getId(), NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("ORGANIZER_CANNOT_BE_REMOVED");
    }

    @Test
    void aMemberWithoutTheApplicationTakesASeat() {
        Membership manual = group.addManualMember("Marta Quispe", "+51923456789", NOW);

        assertThat(manual.getKind()).isEqualTo(MembershipKind.MANUAL);
        assertThat(group.freeSeats()).isEqualTo(1);
    }

    @Test
    void theSeatsCannotGoBelowTheMembersAlreadyIn() {
        fill();

        assertThatThrownBy(() -> group.updateRules("Junta de la familia Weber", rules(2, null), NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("SEATS_BELOW_MEMBERS");
    }

    @Test
    void changingTheRulesIsAnnouncedToTheMembers() {
        group.clearDomainEvents();

        group.updateRules("Junta de la familia Weber", rules(4, null), NOW);

        assertThat(group.getRules().seats()).isEqualTo(4);
        assertThat(group.domainEvents()).hasAtLeastOneElementOfType(RulesUpdatedEvent.class);
    }

    @Test
    void theTurnsMustGiveEveryMemberExactlyOneTurn() {
        fill();
        var first = group.activeMemberships().getFirst().getId();
        var repeated = List.of(new TurnSlot(1, first, TurnMethod.AGREED), new TurnSlot(2, first, TurnMethod.AGREED),
                new TurnSlot(3, group.activeMemberships().get(1).getId(), TurnMethod.AGREED));

        assertThatThrownBy(() -> group.assignTurns(repeated, TurnMethod.AGREED, null, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("INVALID_TURN_ORDER");
    }

    @Test
    void cannotStartUntilFullWithTurnsAndADestination() {
        fill();
        drawTurns();

        assertThat(group.canStart()).isFalse();
        assertThatThrownBy(() -> group.start(NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("SAVINGS_GROUP_NOT_READY");
    }

    @Test
    void startsWhenReadyAndThenItsSetupCanNoLongerChange() {
        fill();
        drawTurns();
        group.defineDestination(yape(), NOW);
        assertThat(group.getStatus()).isEqualTo(GroupStatus.READY);
        group.clearDomainEvents();

        group.start(NOW);

        assertThat(group.getStatus()).isEqualTo(GroupStatus.STARTED);
        assertThat(group.domainEvents()).singleElement().isInstanceOf(GroupStartedEvent.class);
        assertThatThrownBy(() -> group.updateRules("Junta de la familia Weber", rules(3, yape()), NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("SAVINGS_GROUP_ALREADY_STARTED");
    }
}
