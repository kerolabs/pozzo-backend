package pe.kerolabs.pozzo.contributions.application.commandservices;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.commands.AttachReceiptImageCommand;
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
     * Registers the requester's contribution with the data read from the receipt.
     * A receipt that matches settles the contribution at once; one that does not waits for organizer review.
     *
     * @param command command containing period ID, contributor account ID, and payment proof data
     * @return the registered contribution aggregate, or an error if invalid/already settled
     */
    Result<Contribution, ApplicationError> handle(RegisterContributionCommand command);

    /**
     * Registers a direct cash contribution on behalf of any member.
     * Only permitted for the group organizer.
     *
     * @param command command with period ID, organizer account ID, target member ID, and payment details
     * @return the registered and validated contribution aggregate, or an error
     */
    Result<Contribution, ApplicationError> handle(RegisterCashContributionCommand command);

    /**
     * Registers a coverage payment where one member covers the payment for another member.
     * Only permitted for the group organizer.
     *
     * @param command command with period ID, organizer account ID, covering member ID, and covered member ID
     * @return the registered coverage contribution aggregate, or an error
     */
    Result<Contribution, ApplicationError> handle(RegisterCoverageCommand command);

    /**
     * Reviews an inconsistent contribution to approve or reject it.
     * Only permitted for the group organizer.
     *
     * @param command command with contribution ID, organizer account ID, approval decision, and optional rejection reason
     * @return the updated contribution aggregate, or an error
     */
    Result<Contribution, ApplicationError> handle(ReviewContributionCommand command);

    /**
     * Stores and attaches the binary image of the payment receipt to a contribution.
     * Only permitted for the member who registered the contribution.
     *
     * @param command command containing contribution ID, member account ID, raw image bytes, and content type
     * @return the updated contribution aggregate with image path, or an error
     */
    Result<Contribution, ApplicationError> handle(AttachReceiptImageCommand command);
}
