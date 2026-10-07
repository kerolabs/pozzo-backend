package pe.kerolabs.pozzo.iam.domain.model.commands;

/**
 * Command to send a recovery code to the backup email of an account.
 *
 * @param email the backup email the member registered in their profile
 */
public record RequestRecoveryCodeCommand(String email) {
}
