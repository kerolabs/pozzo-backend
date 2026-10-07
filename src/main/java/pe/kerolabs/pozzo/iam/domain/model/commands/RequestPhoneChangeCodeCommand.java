package pe.kerolabs.pozzo.iam.domain.model.commands;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

import java.util.UUID;

/**
 * Command to send an SMS code to the number a signed-in member wants to switch to.
 *
 * @param accountId   the account of the member
 * @param phoneNumber the new number
 */
public record RequestPhoneChangeCodeCommand(UUID accountId, PhoneNumber phoneNumber) {
}
