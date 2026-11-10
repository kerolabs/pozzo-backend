package pe.kerolabs.pozzo.contributions.application.commandservices;

import pe.kerolabs.pozzo.contributions.application.queryservices.AuctionView;
import pe.kerolabs.pozzo.contributions.domain.model.commands.CloseAuctionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.CloseDueAuctionsCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.PlaceBidCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for the auctions of the turns.
 */
public interface AuctionCommandService {

    /**
     * Places or replaces the bid of a member who has not collected.
     *
     * @return the auction with the bid, or an error
     */
    Result<AuctionView, ApplicationError> handle(PlaceBidCommand command);

    /**
     * Closes the auction of a period and gives its turn to the winner. Only the organizer.
     *
     * @return the closed auction, or an error
     */
    Result<AuctionView, ApplicationError> handle(CloseAuctionCommand command);

    /**
     * Closes the auctions whose closing date passed.
     *
     * @return how many auctions closed
     */
    Result<Integer, ApplicationError> handle(CloseDueAuctionsCommand command);
}
