package pe.kerolabs.pozzo.contributions.domain.model.commands;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Gives the turn and the pending contributions of a member who dropped out to their replacement.
 *
 * @param accountId the account of the replacement, or null when they do not use the application
 */
public record ReplaceCycleMemberCommand(UUID groupId, UUID droppedMembershipId, UUID replacementMembershipId,
                                        @Nullable UUID accountId, String displayName) {
}
