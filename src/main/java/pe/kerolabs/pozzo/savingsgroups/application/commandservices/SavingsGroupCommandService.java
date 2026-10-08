package pe.kerolabs.pozzo.savingsgroups.application.commandservices;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AddManualMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.CloseGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.CreateGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.DefineDestinationCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.DeleteGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.GenerateInvitationCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.JoinGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.RemoveMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.StartGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.UpdateRulesCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for the lifecycle of a savings group and its members.
 *
 * <p>Every command that changes the setup of a group can only be run by its organizer. A requester
 * who is not a member gets the same error as for a group that does not exist.</p>
 */
public interface SavingsGroupCommandService {

    /**
     * Creates a new savings group with initial rules, designating the creator as organizer.
     *
     * @param command command containing organizer account ID, title, rules, and optional description
     * @return the created savings group aggregate, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(CreateGroupCommand command);

    /**
     * Updates the rules of an unstarted savings group (e.g. contribution amount, frequency).
     *
     * @param command command containing group ID, organizer account ID, and new rules
     * @return the updated savings group aggregate, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(UpdateRulesCommand command);

    /**
     * Defines the destination bank account or payment method for group contributions.
     *
     * @param command command containing group ID, organizer account ID, and payment destination details
     * @return the updated savings group aggregate, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(DefineDestinationCommand command);

    /**
     * Generates a new invitation and expires any previously active invitation for the group.
     *
     * @param command command containing group ID and organizer account ID
     * @return the created invitation aggregate, or an error
     */
    Result<Invitation, ApplicationError> handle(GenerateInvitationCommand command);

    /**
     * Enrolls an authenticated member into the savings group using an invitation code.
     *
     * @param command command containing invitation code and member account ID
     * @return the joined savings group aggregate, or an error if full, expired, or started
     */
    Result<SavingsGroup, ApplicationError> handle(JoinGroupCommand command);

    /**
     * Registers an offline/manual participant into the group without mobile app access.
     *
     * @param command command containing group ID, organizer account ID, and manual member info
     * @return the updated savings group aggregate, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(AddManualMemberCommand command);

    /**
     * Removes an enrolled member or manual placeholder from the group prior to start.
     *
     * @param command command containing group ID, organizer account ID, and membership ID
     * @return the updated savings group aggregate, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(RemoveMemberCommand command);

    /**
     * Starts the group, locks the roster, expires pending invitations, and readies contribution cycles.
     *
     * @param command command containing group ID and organizer account ID
     * @return the started savings group aggregate, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(StartGroupCommand command);

    /**
     * Deletes a group that has not started, with its members and invitations, and returns it as it was.
     *
     * @param command command containing group ID and organizer account ID
     * @return the deleted savings group state, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(DeleteGroupCommand command);

    /**
     * Closes the group when Contributions reports that its financial cycle is completed.
     *
     * @param command command containing group ID
     * @return the closed savings group aggregate, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(CloseGroupCommand command);
}
