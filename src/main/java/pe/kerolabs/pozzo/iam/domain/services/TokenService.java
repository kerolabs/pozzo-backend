package pe.kerolabs.pozzo.iam.domain.services;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.SessionTokenClaims;

import java.time.Instant;
import java.util.Optional;

/**
 * Issues and reads the tokens that authorize each request.
 *
 * <p>A session token identifies the account and the session of a signed-in member. A registration
 * token proves that a phone number was verified, and lets a new member complete the registration.</p>
 */
public interface TokenService {

    /**
     * Issues a session token.
     */
    String issueSessionToken(SessionTokenClaims claims, Instant issuedAt, Instant expiresAt);

    /**
     * Issues a registration token for a verified phone number.
     */
    String issueRegistrationToken(PhoneNumber phoneNumber, Instant issuedAt, Instant expiresAt);

    /**
     * Reads a session token. Empty when the token is malformed, has an invalid signature,
     * has expired or is not a session token.
     */
    Optional<SessionTokenClaims> readSessionToken(String token);

    /**
     * Reads a registration token. Empty under the same conditions as {@link #readSessionToken(String)}.
     */
    Optional<PhoneNumber> readRegistrationToken(String token);

    /**
     * Hashes a token so the session can be found by it without storing the token itself.
     * The same token always produces the same hash.
     */
    String hash(String token);
}
