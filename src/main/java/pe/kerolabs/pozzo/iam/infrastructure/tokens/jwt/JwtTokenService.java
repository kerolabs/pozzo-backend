package pe.kerolabs.pozzo.iam.infrastructure.tokens.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.SessionTokenClaims;
import pe.kerolabs.pozzo.iam.domain.services.TokenService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Issues and reads JSON Web Tokens signed with the server key (HMAC-SHA).
 *
 * <p>A session token carries the account as subject and the session as the {@code sid} claim.
 * A registration token carries the verified phone number as subject. The {@code typ} claim keeps
 * one kind of token from being used as the other.</p>
 */
@Slf4j
@Service
public class JwtTokenService implements TokenService {

    private static final String TYPE_CLAIM = "typ";
    private static final String SESSION_CLAIM = "sid";
    private static final String SESSION_TYPE = "session";
    private static final String REGISTRATION_TYPE = "registration";

    private final SecretKey signingKey;

    public JwtTokenService(@Value("${authorization.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issueSessionToken(SessionTokenClaims claims, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(claims.accountId().toString())
                .claim(SESSION_CLAIM, claims.sessionId().toString())
                .claim(TYPE_CLAIM, SESSION_TYPE)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String issueRegistrationToken(PhoneNumber phoneNumber, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(phoneNumber.e164())
                .claim(TYPE_CLAIM, REGISTRATION_TYPE)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Optional<SessionTokenClaims> readSessionToken(String token) {
        return parse(token, SESSION_TYPE).map(claims -> new SessionTokenClaims(
                UUID.fromString(claims.getSubject()),
                UUID.fromString(claims.get(SESSION_CLAIM, String.class))));
    }

    @Override
    public Optional<PhoneNumber> readRegistrationToken(String token) {
        return parse(token, REGISTRATION_TYPE).map(claims -> PhoneNumber.fromE164(claims.getSubject()));
    }

    @Override
    public String hash(String token) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private Optional<Claims> parse(String token, String expectedType) {
        try {
            var claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            if (!expectedType.equals(claims.get(TYPE_CLAIM, String.class))) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Rejected {} token: {}", expectedType, e.getMessage());
            return Optional.empty();
        }
    }
}
