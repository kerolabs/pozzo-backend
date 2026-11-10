package pe.kerolabs.pozzo.contributions.interfaces.events;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The auction of a turn opened or closed. Notifications tells the members.
 *
 * @param stage         OPENED or CLOSED
 * @param winner        who won the turn, once it closed
 * @param payoutAmount  what the winner receives, once it closed
 * @param members       every member of the cycle
 */
public record AuctionIntegrationEvent(UUID periodId, UUID cycleId, UUID groupId, String groupName, int turnNumber,
                                      String stage, LocalDate closesOn, BigDecimal minimumBid,
                                      @Nullable CycleMember winner, @Nullable BigDecimal payoutAmount,
                                      List<CycleMember> members, Instant occurredAt) {

    public static final String OPENED = "OPENED";
    public static final String CLOSED = "CLOSED";
}
