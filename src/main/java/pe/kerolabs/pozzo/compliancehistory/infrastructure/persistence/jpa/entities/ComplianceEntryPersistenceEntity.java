package pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities;

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
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for compliance entries. The source event id is unique, so the same event is
 * never counted twice.
 */
@Entity
@Table(name = "compliance_entries", schema = "compliance_history",
        uniqueConstraints = @UniqueConstraint(name = "uq_entries_source", columnNames = "source_event_id"),
        indexes = @Index(name = "ix_entries_member_cycle", columnList = "member_id, cycle_id"))
@Getter
@Setter
@NoArgsConstructor
public class ComplianceEntryPersistenceEntity extends AbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberRecordPersistenceEntity record;

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "group_name", nullable = false, length = 80)
    private String groupName;

    @Column(name = "period_id")
    private UUID periodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 16)
    private EntryKind kind;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "source_event_id", nullable = false, length = 120)
    private String sourceEventId;
}
