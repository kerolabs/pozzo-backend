package pe.kerolabs.pozzo.contributions.interfaces.rest.transform;

import pe.kerolabs.pozzo.contributions.application.queryservices.PeriodView;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ExpectedStatus;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.PeriodStatusResource;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Converts a {@link PeriodView} into {@link PeriodStatusResource}, deriving the state each member sees:
 * VALIDATED or COVERED once settled, IN_REVIEW while the organizer has to review the receipt, and
 * PENDING or LATE (after the cutoff) otherwise.
 */
public class PeriodStatusResourceFromViewAssembler {

    public static final String VALIDATED = "VALIDATED";
    public static final String COVERED = "COVERED";
    public static final String IN_REVIEW = "IN_REVIEW";
    public static final String PENDING = "PENDING";
    public static final String LATE = "LATE";

    public static PeriodStatusResource toResourceFromView(PeriodView view, UUID requesterAccountId, LocalDate today) {
        var cycle = view.cycle();
        var period = view.period();
        var me = cycle.participantOf(requesterAccountId).map(CycleTurn::membershipId).orElse(null);
        var members = cycle.getTurns().stream()
                .map(turn -> new PeriodStatusResource.MemberStatus(
                        turn.membershipId(),
                        turn.displayName(),
                        turn.membershipId().equals(me),
                        turn.membershipId().equals(period.getPayoutMembershipId()),
                        statusOf(view, turn.membershipId(), today),
                        contributionBehind(view, turn.membershipId())))
                .toList();
        var collected = period.collected();
        return new PeriodStatusResource(
                period.getId(),
                cycle.getId(),
                cycle.getGroupName(),
                period.getTurnNumber(),
                cycle.totalTurns(),
                period.getStatus(),
                period.getOpensAt(),
                period.getCutoffDate(),
                ChronoUnit.DAYS.between(today, period.getCutoffDate()),
                period.getPayoutMembershipId(),
                cycle.findParticipant(period.getPayoutMembershipId()).map(CycleTurn::displayName).orElse(""),
                cycle.getRules().contribution().amount(),
                period.potAmount().amount(),
                collected.amount(),
                period.potAmount().minus(collected).amount(),
                period.settledCount(),
                cycle.totalTurns(),
                me == null ? PENDING : statusOf(view, me, today),
                period.getDeliveredAt(),
                members);
    }

    /**
     * The state of a member's contribution in a period, as shown in the application.
     */
    public static String statusOf(PeriodView view, UUID membershipId, LocalDate today) {
        var expected = view.period().expectedFor(membershipId).orElseThrow();
        if (expected.getStatus() == ExpectedStatus.COVERED) {
            return COVERED;
        }
        if (expected.getStatus() == ExpectedStatus.PAID) {
            return VALIDATED;
        }
        if (view.underReviewFor(membershipId).isPresent()) {
            return IN_REVIEW;
        }
        return expected.isLate(today, view.period().getCutoffDate()) ? LATE : PENDING;
    }

    private static UUID contributionBehind(PeriodView view, UUID membershipId) {
        return view.settlingFor(membershipId)
                .or(() -> view.underReviewFor(membershipId))
                .map(Contribution::getId)
                .orElse(null);
    }
}
