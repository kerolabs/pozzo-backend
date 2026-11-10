package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Links a new account to the members the organizers registered without the application with its number.
 *
 * @param phoneNumber the verified number of the account in E.164 format
 */
public record LinkManualMembersCommand(UUID accountId, String phoneNumber) {
}
