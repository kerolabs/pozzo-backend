package pe.kerolabs.pozzo.contributions.application.commandservices;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.model.commands.AttachPayoutProofCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.DeliverPotCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.LinkCycleMemberCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.StartCycleCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.SwapTurnsCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for the lifecycle of a cycle: start it and move from one turn
 * to the next as each pot is delivered.
 */
public interface CycleCommandService {

    /**
     * Starts the financial cycle of a group and opens its first period. Starting it again has no effect.
     *
     * @param command command with group ID and organizer account ID
     * @return the started cycle aggregate, or an error
     */
    Result<Cycle, ApplicationError> handle(StartCycleCommand command);

    /**
     * Confirms the delivery of a pot, then opens the next period or closes the cycle after the last turn.
     *
     * @param command command with period ID and organizer account ID
     * @return pot delivery result containing the cycle, delivered period, and optional next period
     */
    Result<PotDelivery, ApplicationError> handle(DeliverPotCommand command);

    /**
     * Gives the turn of a member who took part without the application to the account they signed up with.
     * Nothing changes when the group has not started its cycle yet.
     *
     * @param command command with the group, the membership and the account
     * @return true when a turn was linked
     */
    Result<Boolean, ApplicationError> handle(LinkCycleMemberCommand command);

    /**
     * Keeps the image of the transfer with which the organizer delivered the pot. Only the organizer, once the
     * pot was delivered.
     *
     * @param command command with the period, the organizer and the image
     * @return the period with its proof, or an error
     */
    Result<Period, ApplicationError> handle(AttachPayoutProofCommand command);

    /**
     * Swaps the turns of two members whose periods have not started. Only the organizer.
     *
     * @param command command with the cycle, the organizer and the two memberships
     * @return the cycle with the new order, or an error
     */
    Result<Cycle, ApplicationError> handle(SwapTurnsCommand command);

    /**
     * What happened after a pot was delivered.
     *
     * @param cycle           the cycle, closed if that was the last turn
     * @param deliveredPeriod the period whose pot was delivered
     * @param nextPeriod      the period that opened, or null when the cycle closed
     */
    record PotDelivery(Cycle cycle, Period deliveredPeriod, Period nextPeriod) {
    }
}
