package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Periodicity;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables.CycleTurnEmbeddable;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity for cycles: the frozen rules of the group and its collection order.
 */
@Entity
@Table(name = "cycles", schema = "contributions",
        uniqueConstraints = @UniqueConstraint(name = "uq_cycles_group", columnNames = "group_id"))
@Getter
@Setter
@NoArgsConstructor
public class CyclePersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "group_name", nullable = false, length = 80)
    private String groupName;

    @Column(name = "organizer_account_id", nullable = false)
    private UUID organizerAccountId;

    @Column(name = "contribution_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal contributionAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicity", nullable = false, length = 10)
    private Periodicity periodicity;

    @Column(name = "cutoff_day", nullable = false)
    private int cutoffDay;

    @Column(name = "first_contribution_date", nullable = false)
    private LocalDate firstContributionDate;

    @Column(name = "destination_method", nullable = false, length = 10)
    private String destinationMethod;

    @Column(name = "destination_phone", nullable = false, length = 20)
    private String destinationPhone;

    @Column(name = "payee_name", nullable = false, length = 80)
    private String payeeName;

    @Column(name = "total_turns", nullable = false)
    private int totalTurns;

    @Column(name = "current_turn", nullable = false)
    private int currentTurn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private CycleStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @ElementCollection
    @CollectionTable(name = "cycle_turns", schema = "contributions", joinColumns = @JoinColumn(name = "cycle_id"))
    private List<CycleTurnEmbeddable> turns = new ArrayList<>();
}
