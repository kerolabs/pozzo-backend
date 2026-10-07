package pe.kerolabs.pozzo.compliancehistory.domain.model.commands;

import java.util.UUID;

/**
 * Command to create a link that shows the requester's compliance summary to someone outside Pozzo.
 */
public record ShareHistoryCommand(UUID accountId) {
}
