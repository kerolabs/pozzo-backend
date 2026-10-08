package pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates;

import lombok.Getter;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.InvitationGeneratedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationCode;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationStatus;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Invitation aggregate root: the code a member types to join a savings group.
 *
 * <p>It is modeled apart from the group because the organizer can replace it without changing
 * the group. Only one invitation per group is active at a time; it lasts thirty days and expires
 * when the group starts.</p>
 */
@Getter
public class Invitation extends AbstractDomainAggregateRoot<Invitation> {

    public static final Duration VALIDITY = Duration.ofDays(30);

    private UUID id;
    private UUID groupId;
    private InvitationCode code;
    private UUID createdBy;
    private Instant createdAt;
    private Instant expiresAt;
    private InvitationStatus status;

    public Invitation() {
    }

    /**
     * Generates an invitation for a group.
     *
     * @param groupId     the group the invitation is for
     * @param organizerId the organizer who generates it
     * @param code        a code not used by any other invitation
     * @param now         the current time
     * @return the active invitation
     */
    public static Invitation generate(UUID groupId, UUID organizerId, InvitationCode code, Instant now) {
        var invitation = new Invitation();
        invitation.id = UUID.randomUUID();
        invitation.groupId = groupId;
        invitation.code = code;
        invitation.createdBy = organizerId;
        invitation.createdAt = now;
        invitation.expiresAt = now.plus(VALIDITY);
        invitation.status = InvitationStatus.ACTIVE;
        invitation.registerDomainEvent(new InvitationGeneratedEvent(invitation.id, groupId, code.value(), now));
        return invitation;
    }

    /**
     * An invitation is usable while it is active and has not reached its expiration date.
     */
    public boolean isUsable(Instant now) {
        return status == InvitationStatus.ACTIVE && now.isBefore(expiresAt);
    }

    public void expire() {
        this.status = InvitationStatus.EXPIRED;
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID groupId, InvitationCode code, UUID createdBy, Instant createdAt,
                             Instant expiresAt, InvitationStatus status) {
        this.id = id;
        this.groupId = groupId;
        this.code = code;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }
}
