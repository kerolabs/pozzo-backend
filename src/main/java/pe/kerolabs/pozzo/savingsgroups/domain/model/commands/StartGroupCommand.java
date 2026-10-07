package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Command to start a group that has every seat taken, its turns and its destination.
 */
public record StartGroupCommand(UUID groupId, UUID requesterId) {
}
