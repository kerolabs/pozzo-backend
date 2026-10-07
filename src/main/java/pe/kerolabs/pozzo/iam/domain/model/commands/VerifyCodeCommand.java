package pe.kerolabs.pozzo.iam.domain.model.commands;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * Command to verify the code a member received by SMS.
 *
 * @param phoneNumber the number the code was sent to
 * @param code        the code typed by the member
 * @param deviceLabel an optional name of the device, used if a session is opened
 */
public record VerifyCodeCommand(PhoneNumber phoneNumber, String code, @Nullable String deviceLabel) {
}
