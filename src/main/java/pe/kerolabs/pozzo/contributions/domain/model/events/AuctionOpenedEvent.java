package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * The auction of a period opened.
 */
public record AuctionOpenedEvent(UUID auctionId, UUID cycleId, UUID periodId, int turnNumber, LocalDate closesOn,
                                 Instant occurredAt) {
}
