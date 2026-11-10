package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Gives the seat and the turn of a member who dropped out to someone else. Only the organizer.
 *
 * @param phoneNumber an optional nine-digit number; when it belongs to an account, the replacement uses it
 */
public record ReplaceMemberCommand(UUID groupId, UUID requesterId, UUID droppedMembershipId, String displayName,
                                   @Nullable String phoneNumber) {
}
