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
     * Orders the members at random with a new seed; running it again repeats the draw.
     */
    Result<SavingsGroup, ApplicationError> handle(AssignTurnsByDrawCommand command);

    Result<SavingsGroup, ApplicationError> handle(AssignTurnsAgreedCommand command);
}
