package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Command to order the turns at random.
 */
public record AssignTurnsByDrawCommand(UUID groupId, UUID requesterId) {
}
