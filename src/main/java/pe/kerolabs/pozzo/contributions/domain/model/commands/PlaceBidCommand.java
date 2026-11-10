package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A member offers a discount on the pot of a period to collect in its turn.
 */
public record PlaceBidCommand(UUID periodId, UUID requesterAccountId, BigDecimal amount) {
}
