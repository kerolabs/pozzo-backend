package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Command to close a group whose cycle is over, once every member has collected.
 */
public record CloseGroupCommand(UUID groupId) {
}
