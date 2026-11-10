package pe.kerolabs.pozzo.contributions.interfaces.rest.transform;

import pe.kerolabs.pozzo.contributions.application.queryservices.VerifiedContributionView;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.VerifiedContributionResource;

/**
 * Builds what a public verification link shows of a contribution.
 */
public final class VerifiedContributionResourceFromViewAssembler {

    private VerifiedContributionResourceFromViewAssembler() {
    }

    public static VerifiedContributionResource toResourceFromView(VerifiedContributionView view) {
        var contribution = view.contribution();
        var receipt = contribution.getReceipt();
        var operation = receipt == null ? null : receipt.operationNumber();
        return new VerifiedContributionResource(
                view.cycle().getGroupName(),
                view.cycle().findParticipant(contribution.getMembershipId()).map(CycleTurn::displayName).orElse(""),
                view.period().getTurnNumber(),
                view.period().getCutoffDate(),
                contribution.getAmount().amount(),
                contribution.getAmount().currency(),
                contribution.getMethod(),
                receipt == null ? null : receipt.paidAt(),
                operation == null ? null : operation.substring(Math.max(0, operation.length() - 4)),
                contribution.getRegisteredAt());
    }
}
