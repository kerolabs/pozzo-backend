package pe.kerolabs.pozzo.contributions.interfaces.rest.transform;

import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterCashContributionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterContributionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterCoverageCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.ReviewContributionCommand;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.RegisterCashContributionResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.RegisterContributionResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.RegisterCoverageResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.ReviewContributionResource;

import java.util.UUID;

/**
 * Translates the request resources of Contributions into commands, adding the authenticated member.
 * The requester never comes from the request body.
 */
public class ContributionCommandFromResourceAssembler {

    public static RegisterContributionCommand toCommandFromResource(UUID periodId, UUID requesterId,
                                                                    RegisterContributionResource resource) {
        return new RegisterContributionCommand(periodId, requesterId, resource.operationNumber(),
                resource.payerName(), resource.payeeName(), resource.amount(), resource.paidAt(), resource.source());
    }

    public static RegisterCashContributionCommand toCommandFromResource(UUID periodId, UUID requesterId,
                                                                        RegisterCashContributionResource resource) {
        return new RegisterCashContributionCommand(periodId, requesterId, resource.membershipId(), resource.amount(),
                resource.receivedOn());
    }

    public static RegisterCoverageCommand toCommandFromResource(UUID periodId, UUID requesterId,
                                                                RegisterCoverageResource resource) {
        return new RegisterCoverageCommand(periodId, requesterId, resource.membershipId(),
                resource.coveredByMembershipId());
    }

    public static ReviewContributionCommand toCommandFromResource(UUID contributionId, UUID requesterId,
                                                                  ReviewContributionResource resource) {
        return new ReviewContributionCommand(contributionId, requesterId, resource.decision(), resource.note());
    }
}
