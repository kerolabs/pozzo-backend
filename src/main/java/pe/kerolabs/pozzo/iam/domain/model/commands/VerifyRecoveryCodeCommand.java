package pe.kerolabs.pozzo.iam.domain.model.commands;

/**
 * Command to verify the code a member received at their backup email.
 *
 * @param email the backup email the code was sent to
 * @param code  the code typed by the member
 */
public record VerifyRecoveryCodeCommand(String email, String code) {
}
