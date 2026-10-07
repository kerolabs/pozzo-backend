package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupStatus;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.PaymentMethod;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Periodicity;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.embeddables.TurnSlotEmbeddable;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity for savings groups. The rules are stored in columns of the same row;
 * the members and the turns live in their own tables.
 */
@Entity
@Table(name = "savings_groups", schema = "savings_groups")
@Getter
@Setter
@NoArgsConstructor
public class SavingsGroupPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(name = "contribution_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal contributionAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicity", nullable = false, length = 10)
    private Periodicity periodicity;

    @Column(name = "cutoff_day", nullable = false)
    private int cutoffDay;

    @Column(name = "seats", nullable = false)
    private int seats;

    @Column(name = "first_contribution_date", nullable = false)
    private LocalDate firstContributionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "destination_method", length = 10)
    private PaymentMethod destinationMethod;

    @Column(name = "destination_phone", length = 20)
    private String destinationPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "turn_method", length = 10)
    private TurnMethod turnMethod;

    @Column(name = "draw_seed", length = 64)
    private String drawSeed;

    @Column(name = "turns_assigned_at")
    private Instant turnsAssignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private GroupStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "started_at")
    private Instant startedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<MembershipPersistenceEntity> memberships = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "turn_slots", schema = "savings_groups", joinColumns = @JoinColumn(name = "group_id"))
    private List<TurnSlotEmbeddable> turns = new ArrayList<>();
}
