package pe.kerolabs.pozzo.contributions.application.queryservices;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A period together with its cycle and its contributions, enough to tell the state of each member.
 *
 * @param cycle         the cycle the period belongs to
 * @param period        the period
 * @param contributions the contributions registered in the period, the most recent first
 */
public record PeriodView(Cycle cycle, Period period, List<Contribution> contributions) {

    /**
     * The contribution of a member that waits for the organizer's review, if any.
     */
    public Optional<Contribution> underReviewFor(UUID membershipId) {
        return contributions.stream()
                .filter(contribution -> contribution.getMembershipId().equals(membershipId))
                .filter(Contribution::isUnderReview)
                .findFirst();
    }

    /**
     * The contribution that settled what a member owed, if any.
     */
    public Optional<Contribution> settlingFor(UUID membershipId) {
        return period.expectedFor(membershipId)
                .map(expected -> expected.getSettledByContributionId())
                .flatMap(contributionId -> contributions.stream()
                        .filter(contribution -> contribution.getId().equals(contributionId))
                        .findFirst());
    }
}
