package pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates;

import lombok.Getter;
import pe.kerolabs.pozzo.compliancehistory.domain.model.events.HistorySharedEvent;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ShareToken;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * ShareLink aggregate root: a link that shows a member's compliance summary to someone outside Pozzo,
 * valid for seven days unless the member revokes it first.
 */
@Getter
public class ShareLink extends AbstractDomainAggregateRoot<ShareLink> {

    public static final Duration VALIDITY = Duration.ofDays(7);

    private UUID id;
    private ShareToken token;
    private UUID accountId;
    private Instant createdAt;
    private Instant expiresAt;
    private boolean revoked;

    public ShareLink() {
    }

    public static ShareLink issue(UUID accountId, Instant now) {
        var link = new ShareLink();
        link.id = UUID.randomUUID();
        link.token = ShareToken.random();
        link.accountId = accountId;
        link.createdAt = now;
        link.expiresAt = now.plus(VALIDITY);
        link.revoked = false;
        link.registerDomainEvent(new HistorySharedEvent(accountId, link.expiresAt, now));
        return link;
    }

    public boolean isValid(Instant now) {
        return !revoked && now.isBefore(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, ShareToken token, UUID accountId, Instant createdAt, Instant expiresAt,
                             boolean revoked) {
        this.id = id;
        this.token = token;
        this.accountId = accountId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }
}
