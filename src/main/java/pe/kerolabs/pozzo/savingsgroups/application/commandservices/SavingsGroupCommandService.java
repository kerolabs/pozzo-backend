package pe.kerolabs.pozzo.savingsgroups.application.commandservices;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AddManualMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.CloseGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.CreateGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.DefineDestinationCommand;
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

    Result<SavingsGroup, ApplicationError> handle(CreateGroupCommand command);

    Result<SavingsGroup, ApplicationError> handle(UpdateRulesCommand command);

    Result<SavingsGroup, ApplicationError> handle(DefineDestinationCommand command);

    /**
     * Generates a new invitation and expires the previous one.
     */
    Result<Invitation, ApplicationError> handle(GenerateInvitationCommand command);

    Result<SavingsGroup, ApplicationError> handle(JoinGroupCommand command);

    Result<SavingsGroup, ApplicationError> handle(AddManualMemberCommand command);

    Result<SavingsGroup, ApplicationError> handle(RemoveMemberCommand command);

    /**
     * Starts the group and expires its invitation.
     */
    Result<SavingsGroup, ApplicationError> handle(StartGroupCommand command);

    /**
     * Closes the group when Contributions reports that its cycle is over.
     */
    Result<SavingsGroup, ApplicationError> handle(CloseGroupCommand command);
}
