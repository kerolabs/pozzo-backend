package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Command to register a member who does not use the application.
 */
public record AddManualMemberCommand(UUID groupId, UUID requesterId, String displayName, @Nullable String phoneNumber) {
}
