package pe.kerolabs.pozzo.savingsgroups.application.commandservices;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AssignTurnsAgreedCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AssignTurnsByDrawCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for the collection order. Only the organizer assigns the turns,
 * and only while the group has not started.
 */
public interface TurnCommandService {

    /**
     * Orders the members at random using a newly generated seed; running it again repeats the draw.
     *
     * @param command command containing group ID and organizer account ID
     * @return the savings group aggregate with updated turns, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(AssignTurnsByDrawCommand command);

    /**
     * Sets a manually agreed turn order for all enrolled members.
     *
     * @param command command containing group ID, organizer account ID, and explicit membership ID list
     * @return the savings group aggregate with updated turns, or an error
     */
    Result<SavingsGroup, ApplicationError> handle(AssignTurnsAgreedCommand command);
}
