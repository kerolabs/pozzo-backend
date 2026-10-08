package pe.kerolabs.pozzo.iam.domain.model.valueobjects;

import java.util.UUID;

/**
 * What a session token says about its bearer: the account and the session it belongs to.
 *
 * @param accountId the account of the member
 * @param sessionId the session opened on the member's device
 */
public record SessionTokenClaims(UUID accountId, UUID sessionId) {
}
