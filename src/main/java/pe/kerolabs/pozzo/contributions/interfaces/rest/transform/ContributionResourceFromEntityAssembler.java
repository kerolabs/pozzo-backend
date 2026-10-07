package pe.kerolabs.pozzo.contributions.interfaces.rest.transform;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.ContributionResource;

/**
 * Converts a {@link Contribution} into {@link ContributionResource}.
 */
public class ContributionResourceFromEntityAssembler {

    public static ContributionResource toResourceFromEntity(Contribution contribution, Cycle cycle) {
        var receipt = contribution.getReceipt() == null ? null : new ContributionResource.Receipt(
                contribution.getReceipt().operationNumber(),
                contribution.getReceipt().payerName(),
                contribution.getReceipt().payeeName(),
                contribution.getReceipt().amount().amount(),
                contribution.getReceipt().paidAt(),
                contribution.getReceipt().source());
        var review = contribution.getReview() == null ? null : new ContributionResource.ReviewItem(
                contribution.getReview().decision(),
                contribution.getReview().note(),
                contribution.getReview().reviewedAt());
        return new ContributionResource(
                contribution.getId(),
                contribution.getPeriodId(),
                contribution.getMembershipId(),
                cycle.findParticipant(contribution.getMembershipId()).map(CycleTurn::displayName).orElse(""),
                contribution.getAmount().amount(),
                contribution.getMethod(),
                contribution.getStatus(),
                receipt,
                contribution.getInconsistencies().stream()
                        .map(item -> new ContributionResource.InconsistencyItem(item.field(), item.expected(), item.found()))
                        .toList(),
                contribution.getCoveredByMembershipId(),
                review,
                contribution.getRegisteredAt());
    }
}
