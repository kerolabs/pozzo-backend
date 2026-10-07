package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * Command for the organizer to confirm that the pot of a period was handed to the member who collects.
 */
public record DeliverPotCommand(UUID periodId, UUID requesterAccountId) {
}
