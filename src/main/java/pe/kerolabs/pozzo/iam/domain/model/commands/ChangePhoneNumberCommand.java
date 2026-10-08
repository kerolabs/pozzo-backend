package pe.kerolabs.pozzo.iam.domain.model.commands;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

import java.util.UUID;

/**
 * Command to link the account of a signed-in member to a new number.
 *
 * @param accountId   the account of the member
 * @param phoneNumber the new number
 * @param code        the SMS code received at the new number
 */
public record ChangePhoneNumberCommand(UUID accountId, PhoneNumber phoneNumber, String code) {
}
