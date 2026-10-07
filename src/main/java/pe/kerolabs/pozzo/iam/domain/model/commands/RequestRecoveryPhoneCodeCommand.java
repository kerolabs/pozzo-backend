package pe.kerolabs.pozzo.iam.domain.model.commands;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * Command to send an SMS code to the new number of an account being recovered.
 *
 * @param recoveryToken the token received after verifying the backup email
 * @param phoneNumber   the new number to link to the account
 */
public record RequestRecoveryPhoneCodeCommand(String recoveryToken, PhoneNumber phoneNumber) {
}
