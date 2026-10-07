package pe.kerolabs.pozzo.compliancehistory.domain.model.commands;

import java.util.UUID;

/**
 * Command to revoke one of the requester's share links.
 */
public record RevokeShareLinkCommand(String token, UUID accountId) {
}
