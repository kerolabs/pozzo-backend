package pe.kerolabs.pozzo.contributions.application.queryservices;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Auction;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;

/**
 * An auction with its cycle and period.
 */
public record AuctionView(Auction auction, Cycle cycle, Period period) {
}
