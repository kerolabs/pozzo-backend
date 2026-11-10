package pe.kerolabs.pozzo.contributions.domain.model.commands;

/**
 * Closes the auctions whose closing date passed: with the highest bid, or by draw when nobody bid and the group
 * decides so. The ones that wait for the organizer to choose stay open.
 */
public record CloseDueAuctionsCommand() {
}
