package pe.kerolabs.pozzo.contributions.application.commandservices;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterCashContributionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterContributionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterCoverageCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.ReviewContributionCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for registering and reviewing contributions.
 *
 * <p>A member registers only their own contribution, identified by the token. Cash, coverage and
 * reviews are reserved to the organizer. A requester who does not take part in the cycle gets the
 * same error as for a period that does not exist.</p>
 */
public interface ContributionCommandService {

    /**
     * Registers the requester's contribution with the data read from the receipt. A receipt that
     * matches settles the contribution at once; one that does not waits for the organizer.
     */
    Result<Contribution, ApplicationError> handle(RegisterContributionCommand command);

    Result<Contribution, ApplicationError> handle(RegisterCashContributionCommand command);

    Result<Contribution, ApplicationError> handle(RegisterCoverageCommand command);

    Result<Contribution, ApplicationError> handle(ReviewContributionCommand command);
}
