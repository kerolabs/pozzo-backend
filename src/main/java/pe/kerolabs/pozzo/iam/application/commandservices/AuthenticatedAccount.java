package pe.kerolabs.pozzo.iam.application.commandservices;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;

import java.time.Instant;

/**
 * A signed-in member: the account and the token of the session just opened.
 *
 * @param account   the account of the member
 * @param token     the session token the client sends on every request
 * @param expiresAt the moment the session ends if not revoked
 */
public record AuthenticatedAccount(Account account, String token, Instant expiresAt) {
}
