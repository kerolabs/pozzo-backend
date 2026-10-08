package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Command to remove a member before the group starts.
 */
public record RemoveMemberCommand(UUID groupId, UUID requesterId, UUID membershipId) {
}
