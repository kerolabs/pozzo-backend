package pe.kerolabs.pozzo.iam.domain.model.aggregates;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.iam.domain.model.events.SessionOpenedEvent;
import pe.kerolabs.pozzo.iam.domain.model.events.SessionRevokedEvent;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Session aggregate root: a member signed in on a device. It is stored by the hash of its token,
 * so it can be revoked, and lasts thirty days unless the member signs out first.
 */
@Getter
public class Session extends AbstractDomainAggregateRoot<Session> {

    public static final Duration VALIDITY = Duration.ofDays(30);

    private UUID id;
    private UUID accountId;
    private String tokenHash;
    private @Nullable String deviceLabel;
    private Instant issuedAt;
    private Instant expiresAt;
    private @Nullable Instant revokedAt;

    public Session() {
    }

    /**
     * Opens a session. The identifier is chosen beforehand because the token carries it.
     *
     * @param id          the session identifier, also present in the token
     * @param accountId   the account that signs in
     * @param tokenHash   the hash of the session token
     * @param deviceLabel an optional name of the device, e.g. "Pixel 8"
     * @param issuedAt    the moment the session opens
     * @param expiresAt   the moment the session ends if not revoked
     * @return the open session
     */
    public static Session open(UUID id, UUID accountId, String tokenHash, @Nullable String deviceLabel,
                               Instant issuedAt, Instant expiresAt) {
        var session = new Session();
        session.id = id;
        session.accountId = accountId;
        session.tokenHash = tokenHash;
        session.deviceLabel = deviceLabel;
        session.issuedAt = issuedAt;
        session.expiresAt = expiresAt;
        session.registerDomainEvent(new SessionOpenedEvent(id, accountId, issuedAt));
        return session;
    }

    public boolean isActive(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt);
    }

    /**
     * Closes the session. Revoking an already revoked session has no effect.
     */
    public void revoke(Instant now) {
        if (revokedAt != null) {
            return;
        }
        this.revokedAt = now;
        registerDomainEvent(new SessionRevokedEvent(id, accountId, now));
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID accountId, String tokenHash, @Nullable String deviceLabel,
                             Instant issuedAt, Instant expiresAt, @Nullable Instant revokedAt) {
        this.id = id;
        this.accountId = accountId;
        this.tokenHash = tokenHash;
        this.deviceLabel = deviceLabel;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }
}
