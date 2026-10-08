package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Command to generate a new invitation code for a group; it replaces the active one.
 */
public record GenerateInvitationCommand(UUID groupId, UUID requesterId) {
}
