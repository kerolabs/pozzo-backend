package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.List;
import java.util.UUID;

/**
 * Command to set the order the group agreed on: the first membership collects first.
 */
public record AssignTurnsAgreedCommand(UUID groupId, UUID requesterId, List<UUID> order) {
}
