package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Command to join a savings group with an invitation code.
 */
public record JoinGroupCommand(String invitationCode, UUID memberId) {
}
