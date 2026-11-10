package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Auction;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Bid;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.repositories.AuctionRepository;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables.BidEmbeddable;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.AuctionPersistenceEntity;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.repositories.AuctionPersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persists the auctions and publishes their domain events once saved.
 */
@Repository
public class AuctionRepositoryImpl implements AuctionRepository {

    private final AuctionPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AuctionRepositoryImpl(AuctionPersistenceRepository persistenceRepository,
                                 ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Auction> findByPeriodId(UUID periodId) {
        return persistenceRepository.findByPeriodId(periodId).map(AuctionRepositoryImpl::toDomain);
    }

    @Override
    public List<Auction> findAllOpen() {
        return persistenceRepository.findAllByStatus(AuctionStatus.OPEN).stream()
                .map(AuctionRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public Auction save(Auction auction) {
        var entity = persistenceRepository.findById(auction.getId()).orElseGet(AuctionPersistenceEntity::new);
        entity.setId(auction.getId());
        entity.setCycleId(auction.getCycleId());
        entity.setPeriodId(auction.getPeriodId());
        entity.setTurnNumber(auction.getTurnNumber());
        entity.setClosesOn(auction.getClosesOn());
        entity.setCurrency(auction.getPotAmount().currency());
        entity.setMinimumBid(auction.getMinimumBid().amount());
        entity.setPotAmount(auction.getPotAmount().amount());
        entity.setFallback(auction.getFallback());
        entity.setStatus(auction.getStatus());
        entity.setWinnerMembershipId(auction.getWinnerMembershipId());
        entity.setDiscount(auction.getDiscount() == null ? null : auction.getDiscount().amount());
        entity.setCreditPerMember(auction.getCreditPerMember() == null ? null : auction.getCreditPerMember().amount());
        entity.setResolution(auction.getResolution());
        entity.setClosedAt(auction.getClosedAt());
        entity.getBids().clear();
        auction.getBids().forEach(bid -> entity.getBids().add(
                new BidEmbeddable(bid.membershipId(), bid.amount().amount(), bid.placedAt())));
        var saved = toDomain(persistenceRepository.save(entity));
        auction.domainEvents().forEach(eventPublisher::publishEvent);
        auction.clearDomainEvents();
        return saved;
    }

    private static Auction toDomain(AuctionPersistenceEntity entity) {
        var currency = entity.getCurrency();
        var auction = new Auction();
        auction.restoreState(entity.getId(), entity.getCycleId(), entity.getPeriodId(), entity.getTurnNumber(),
                entity.getClosesOn(), Money.of(entity.getMinimumBid(), currency),
                Money.of(entity.getPotAmount(), currency), entity.getFallback(), entity.getStatus(),
                entity.getBids().stream()
                        .map(bid -> new Bid(bid.getMembershipId(), Money.of(bid.getAmount(), currency),
                                bid.getPlacedAt()))
                        .toList(),
                entity.getWinnerMembershipId(),
                entity.getDiscount() == null ? null : Money.of(entity.getDiscount(), currency),
                entity.getCreditPerMember() == null ? null : Money.of(entity.getCreditPerMember(), currency),
                entity.getResolution(), entity.getClosedAt());
        return auction;
    }
}
