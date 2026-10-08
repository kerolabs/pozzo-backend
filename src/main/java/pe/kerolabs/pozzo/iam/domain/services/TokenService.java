package pe.kerolabs.pozzo.iam.domain.services;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.SessionTokenClaims;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service contract for issuing, verifying, and decoding JSON Web Tokens (JWT)
 * that authenticate and authorize incoming requests.
 *
 * <p>Supports three distinct token lifecycles:
 * <ul>
 *   <li><b>Session Tokens</b>: Identifies active account and session ID for authenticated members.</li>
 *   <li><b>Registration Tokens</b>: Issued upon SMS code verification to allow profile creation.</li>
 *   <li><b>Recovery Tokens</b>: Issued upon email verification to allow updating linked mobile numbers.</li>
 * </ul>
 * </p>
 */
public interface TokenService {

    /**
     * Issues a cryptographically signed session JWT for an authenticated member.
     *
     * @param claims session claims containing account ID, session ID, and role metadata
     * @param issuedAt issuance timestamp
     * @param expiresAt token expiration timestamp
     * @return the serialized signed JWT string
     */
    String issueSessionToken(SessionTokenClaims claims, Instant issuedAt, Instant expiresAt);

    /**
     * Issues a short-lived registration token proving that the given phone number was successfully verified.
     *
     * @param phoneNumber the verified Peruvian mobile phone number
     * @param issuedAt issuance timestamp
     * @param expiresAt token expiration timestamp
     * @return the serialized signed registration JWT
     */
    String issueRegistrationToken(PhoneNumber phoneNumber, Instant issuedAt, Instant expiresAt);

    /**
     * Validates and parses a session JWT.
     *
     * @param token raw JWT token string from Authorization header
     * @return optional containing parsed {@link SessionTokenClaims}, or empty if token is expired, tampered, or invalid
     */
    Optional<SessionTokenClaims> readSessionToken(String token);

    /**
     * Validates and parses a registration JWT.
     *
     * @param token raw registration token string
     * @return optional containing the verified {@link PhoneNumber}, or empty if invalid or expired
     */
    Optional<PhoneNumber> readRegistrationToken(String token);

    /**
     * Issues a short-lived recovery token confirming that the account owner verified access via backup email.
     *
     * @param accountId the account ID being recovered
     * @param issuedAt issuance timestamp
     * @param expiresAt token expiration timestamp
     * @return serialized recovery token string
     */
    String issueRecoveryToken(UUID accountId, Instant issuedAt, Instant expiresAt);

    /**
     * Validates and parses an account recovery token.
     *
     * @param token raw recovery token string
     * @return optional containing the recovered {@link UUID} account ID, or empty if invalid
     */
    Optional<UUID> readRecoveryToken(String token);

    /**
     * Computes a deterministic SHA-256 hash of a token for secure database indexing
     * without persisting raw bearer tokens.
     *
     * @param token the bearer token to hash
     * @return hex-encoded or Base64 hash string
     */
    String hash(String token);
}
