package pe.kerolabs.pozzo.iam.domain.model.commands;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * Command to link a verified new number to an account being recovered and open a session.
 *
 * @param recoveryToken the token received after verifying the backup email
 * @param phoneNumber   the new number
 * @param code          the SMS code received at the new number
 * @param deviceLabel   an optional name of the device for the new session
 */
public record RecoverAccountCommand(String recoveryToken, PhoneNumber phoneNumber, String code,
                                    @Nullable String deviceLabel) {
}
