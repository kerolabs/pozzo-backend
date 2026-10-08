package pe.kerolabs.pozzo.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to close the session of the authenticated member on the current device.
 *
 * @param accountId the account of the member
 * @param sessionId the session to close
 */
public record SignOutCommand(UUID accountId, UUID sessionId) {
}
