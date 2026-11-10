package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Records that a member left a group in progress. Only the organizer.
 */
public record RegisterDropoutCommand(UUID groupId, UUID requesterId, UUID membershipId) {
}
