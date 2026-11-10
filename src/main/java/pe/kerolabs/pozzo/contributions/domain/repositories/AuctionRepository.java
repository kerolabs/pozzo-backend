package pe.kerolabs.pozzo.contributions.domain.repositories;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Auction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository of the auctions of the periods.
 */
public interface AuctionRepository {

    Optional<Auction> findByPeriodId(UUID periodId);

    /**
     * The auctions that have not closed.
     */
    List<Auction> findAllOpen();

    Auction save(Auction auction);
}
