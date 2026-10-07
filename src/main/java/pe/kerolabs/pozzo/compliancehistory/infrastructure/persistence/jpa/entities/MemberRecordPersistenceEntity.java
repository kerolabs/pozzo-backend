package pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceLevel;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity for member records. The key is the member's account, and the counts and the
 * level are stored already computed, so an organizer's query does not walk through every entry.
 */
@Entity
@Table(name = "member_records", schema = "compliance_history")
@AttributeOverride(name = "id", column = @Column(name = "member_id"))
@Getter
@Setter
@NoArgsConstructor
public class MemberRecordPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "on_time_count", nullable = false)
    private int onTimeCount;

    @Column(name = "late_count", nullable = false)
    private int lateCount;

    @Column(name = "covered_count", nullable = false)
    private int coveredCount;

    @Column(name = "rejected_count", nullable = false)
    private int rejectedCount;

    @Column(name = "dropout_count", nullable = false)
    private int dropoutCount;

    @Column(name = "cycles_completed", nullable = false)
    private int cyclesCompleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 10)
    private ComplianceLevel level;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL)
    private List<ComplianceEntryPersistenceEntity> entries = new ArrayList<>();
}
