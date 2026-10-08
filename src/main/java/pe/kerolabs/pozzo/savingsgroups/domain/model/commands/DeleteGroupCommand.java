package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Command to delete a group that has not started, with its members and invitations.
 */
public record DeleteGroupCommand(UUID groupId, UUID requesterId) {
}
