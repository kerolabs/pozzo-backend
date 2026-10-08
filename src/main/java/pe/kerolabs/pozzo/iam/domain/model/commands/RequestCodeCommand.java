package pe.kerolabs.pozzo.iam.domain.model.commands;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * Command to send a verification code to a phone number.
 *
 * @param phoneNumber the number that receives the code
 */
public record RequestCodeCommand(PhoneNumber phoneNumber) {
}
