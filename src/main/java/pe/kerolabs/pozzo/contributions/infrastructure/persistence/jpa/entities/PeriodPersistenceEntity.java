package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.PeriodStatus;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity for periods, with what each member owes in {@code expected_contributions}.
 */
@Entity
@Table(name = "periods", schema = "contributions",
        uniqueConstraints = @UniqueConstraint(name = "uq_periods_turn", columnNames = {"cycle_id", "turn_number"}),
        indexes = @Index(name = "ix_periods_cycle_status", columnList = "cycle_id, status"))
@Getter
@Setter
@NoArgsConstructor
public class PeriodPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;

    @Column(name = "opens_at", nullable = false)
    private LocalDate opensAt;

    @Column(name = "cutoff_date", nullable = false)
    private LocalDate cutoffDate;

    @Column(name = "payout_membership_id", nullable = false)
    private UUID payoutMembershipId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private PeriodStatus status;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "delivered_by")
    private UUID deliveredBy;

    @OneToMany(mappedBy = "period", cascade = CascadeType.ALL)
    private List<ExpectedContributionPersistenceEntity> expected = new ArrayList<>();
}
