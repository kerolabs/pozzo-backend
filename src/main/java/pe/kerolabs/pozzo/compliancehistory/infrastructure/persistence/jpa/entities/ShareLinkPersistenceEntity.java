package pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for share links.
 */
@Entity
@Table(name = "share_links", schema = "compliance_history",
        uniqueConstraints = @UniqueConstraint(name = "uq_share_links_token", columnNames = "token"),
        indexes = @Index(name = "ix_share_links_member", columnList = "member_id, expires_at"))
@Getter
@Setter
@NoArgsConstructor
public class ShareLinkPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "token", nullable = false, length = 32)
    private String token;

    @Column(name = "member_id", nullable = false)
    private UUID accountId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked;
}
