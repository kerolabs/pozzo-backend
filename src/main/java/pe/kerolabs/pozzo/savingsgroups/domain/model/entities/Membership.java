package pe.kerolabs.pozzo.savingsgroups.domain.model.entities;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipKind;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * A member within a savings group. It only makes sense inside its group: the same person is a
 * different membership in each group. A member without the application has no account
 * ({@code memberId} is null), so no account can act on that member's behalf.
 */
@Getter
public class Membership {

    private UUID id;
    private @Nullable UUID memberId;
    private String displayName;
    private @Nullable String phone;
    private MembershipKind kind;
    private MembershipStatus status;
    private Instant joinedAt;

    public Membership() {
    }

    /**
     * Creates the membership of a member who uses the application.
     */
    public static Membership ofAppMember(UUID memberId, String displayName, Instant now) {
        var membership = new Membership();
        membership.id = UUID.randomUUID();
        membership.memberId = memberId;
        membership.displayName = requireName(displayName);
        membership.kind = MembershipKind.APP;
        membership.status = MembershipStatus.ACTIVE;
        membership.joinedAt = now;
        return membership;
    }

    /**
     * Creates the membership of a member registered by the organizer, who does not use the application.
     *
     * @param phone an optional Peruvian mobile number in E.164 format
     */
    public static Membership ofManualMember(String displayName, @Nullable String phone, Instant now) {
        if (phone != null && !phone.matches("\\+519\\d{8}")) {
            throw new IllegalArgumentException("The phone must be a Peruvian mobile number");
        }
        var membership = new Membership();
        membership.id = UUID.randomUUID();
        membership.displayName = requireName(displayName);
        membership.phone = phone;
        membership.kind = MembershipKind.MANUAL;
        membership.status = MembershipStatus.ACTIVE;
        membership.joinedAt = now;
        return membership;
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE || status == MembershipStatus.REPLACEMENT;
    }

    public boolean belongsTo(UUID accountId) {
        return memberId != null && memberId.equals(accountId);
    }

    public void remove() {
        this.status = MembershipStatus.REMOVED;
    }

    public boolean isRemoved() {
        return status == MembershipStatus.REMOVED;
    }

    /**
     * Brings back a member the organizer removed, who joins again with an invitation. The membership keeps
     * its id, so a member has one membership per group.
     */
    public void rejoin(String displayName, Instant now) {
        if (!isRemoved()) {
            throw new IllegalStateException("Only a removed membership can join again");
        }
        this.displayName = requireName(displayName);
        this.status = MembershipStatus.ACTIVE;
        this.joinedAt = now;
    }

    public void markDropped() {
        this.status = MembershipStatus.DROPPED;
    }

    /**
     * Restores the entity from persistence.
     */
    public void restoreState(UUID id, @Nullable UUID memberId, String displayName, @Nullable String phone,
                             MembershipKind kind, MembershipStatus status, Instant joinedAt) {
        this.id = id;
        this.memberId = memberId;
        this.displayName = displayName;
        this.phone = phone;
        this.kind = kind;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    private static String requireName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("The name of the member is required");
        }
        var name = displayName.strip();
        if (name.length() > 80) {
            throw new IllegalArgumentException("The name of the member cannot exceed 80 characters");
        }
        return name;
    }
}
