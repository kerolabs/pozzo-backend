package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipKind;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipStatus;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for the members of a savings group. {@code member_id} is null for the
 * members registered by the organizer, who do not use the application.
 */
@Entity
@Table(name = "memberships", schema = "savings_groups",
        uniqueConstraints = @UniqueConstraint(name = "uq_memberships_member", columnNames = {"group_id", "member_id"}),
        indexes = @Index(name = "ix_memberships_member", columnList = "member_id"))
@Getter
@Setter
@NoArgsConstructor
public class MembershipPersistenceEntity extends AbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private SavingsGroupPersistenceEntity group;

    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 10)
    private MembershipKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private MembershipStatus status;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;
}
