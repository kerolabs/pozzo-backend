package pe.kerolabs.pozzo.notifications.domain.model.aggregates;

import lombok.Getter;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.Platform;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.UUID;

/**
 * Device aggregate root: a phone of a member that receives push notifications through its
 * Firebase Cloud Messaging token. It is deactivated when the member removes it or FCM reports
 * that the token is no longer valid.
 */
@Getter
public class Device extends AbstractDomainAggregateRoot<Device> {

    public static final int TOKEN_MAX_LENGTH = 255;

    private UUID id;
    private UUID accountId;
    private String pushToken;
    private Platform platform;
    private Instant registeredAt;
    private boolean active;

    public Device() {
    }

    public static Device register(UUID accountId, String pushToken, Platform platform, Instant now) {
        var device = new Device();
        device.id = UUID.randomUUID();
        device.accountId = accountId;
        device.pushToken = requireToken(pushToken);
        device.platform = platform;
        device.registeredAt = now;
        device.active = true;
        return device;
    }

    /**
     * Gives an existing token to a member: the same phone after another member signed in, or the
     * same member registering again. The device becomes active.
     */
    public void assignTo(UUID accountId, Platform platform, Instant now) {
        this.accountId = accountId;
        this.platform = platform;
        this.registeredAt = now;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID accountId, String pushToken, Platform platform, Instant registeredAt,
                             boolean active) {
        this.id = id;
        this.accountId = accountId;
        this.pushToken = pushToken;
        this.platform = platform;
        this.registeredAt = registeredAt;
        this.active = active;
    }

    private static String requireToken(String pushToken) {
        if (pushToken == null || pushToken.isBlank() || pushToken.length() > TOKEN_MAX_LENGTH) {
            throw new IllegalArgumentException("The push token is required and cannot exceed 255 characters");
        }
        return pushToken.strip();
    }
}
