package pe.kerolabs.pozzo.iam.domain.model.queries;

/**
 * Query that validates a session token: correct signature, not expired, and its session still active.
 *
 * @param token the session token sent by the client
 */
public record ValidateTokenQuery(String token) {
}
