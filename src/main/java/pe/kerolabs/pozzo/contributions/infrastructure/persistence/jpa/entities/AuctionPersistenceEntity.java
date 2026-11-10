package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionFallback;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionResolution;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionStatus;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables.BidEmbeddable;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity for the auction of a period, with its bids.
 */
@Entity
@Table(name = "auctions", schema = "contributions",
        uniqueConstraints = @UniqueConstraint(name = "uq_auctions_period", columnNames = "period_id"),
        indexes = @Index(name = "ix_auctions_status", columnList = "status"))
@Getter
@Setter
@NoArgsConstructor
public class AuctionPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;

    @Column(name = "closes_on", nullable = false)
    private LocalDate closesOn;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "minimum_bid", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumBid;

    @Column(name = "pot_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal potAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "fallback", nullable = false, length = 10)
    private AuctionFallback fallback;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private AuctionStatus status;

    @Column(name = "winner_membership_id")
    private UUID winnerMembershipId;

    @Column(name = "discount", precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(name = "credit_per_member", precision = 12, scale = 2)
    private BigDecimal creditPerMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution", length = 10)
    private AuctionResolution resolution;

    @Column(name = "closed_at")
    private Instant closedAt;

    @ElementCollection
    @CollectionTable(name = "auction_bids", schema = "contributions", joinColumns = @JoinColumn(name = "auction_id"))
    private List<BidEmbeddable> bids = new ArrayList<>();
}
