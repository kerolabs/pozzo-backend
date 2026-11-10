package pe.kerolabs.pozzo.contributions.application.internal.commandservices;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.contributions.application.commandservices.AuctionCommandService;
import pe.kerolabs.pozzo.contributions.application.queryservices.AuctionView;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Auction;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.model.commands.CloseAuctionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.CloseDueAuctionsCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.PlaceBidCommand;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionFallback;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionResolution;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.repositories.AuctionRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.CycleRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.PeriodRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Takes the bids, closes the auctions and gives each turn to its winner.
 */
@Service
@Transactional
public class AuctionCommandServiceImpl implements AuctionCommandService {

    private static final ZoneId GROUP_ZONE = ZoneId.of("America/Lima");

    private final CycleRepository cycleRepository;
    private final PeriodRepository periodRepository;
    private final AuctionRepository auctionRepository;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public AuctionCommandServiceImpl(CycleRepository cycleRepository, PeriodRepository periodRepository,
                                     AuctionRepository auctionRepository, Clock clock) {
        this.cycleRepository = cycleRepository;
        this.periodRepository = periodRepository;
        this.auctionRepository = auctionRepository;
        this.clock = clock;
    }

    @Override
    public Result<AuctionView, ApplicationError> handle(PlaceBidCommand command) {
        return CycleAccess.requireParticipant(cycleRepository, periodRepository, command.periodId(),
                        command.requesterAccountId())
                .flatMap(access -> auctionOf(access.period()).map(auction -> {
                    var cycle = access.cycle();
                    var bidder = cycle.participantOf(command.requesterAccountId()).orElseThrow();
                    var eligible = cycle.stillToCollect().stream().map(CycleTurn::membershipId).toList();
                    auction.placeBid(bidder.membershipId(), Money.of(command.amount(),
                                    cycle.getRules().contribution().currency()), eligible, today(), clock.instant());
                    return new AuctionView(auctionRepository.save(auction), cycle, access.period());
                }));
    }

    @Override
    public Result<AuctionView, ApplicationError> handle(CloseAuctionCommand command) {
        return CycleAccess.requireOrganizer(cycleRepository, periodRepository, command.periodId(),
                        command.requesterAccountId())
                .flatMap(access -> auctionOf(access.period()).map(auction -> {
                    var cycle = access.cycle();
                    if (!auction.isOpen()) {
                        throw new BusinessRuleViolationException("AUCTION_CLOSED", "The auction of this turn is closed");
                    }
                    if (auction.highestBid().isPresent()) {
                        return finish(auction, cycle, access.period(), auction.highestBid().get().membershipId(),
                                AuctionResolution.BID);
                    }
                    if (command.winnerMembershipId() != null) {
                        return finish(auction, cycle, access.period(), command.winnerMembershipId(),
                                AuctionResolution.ORGANIZER);
                    }
                    if (auction.getFallback() == AuctionFallback.ORGANIZER) {
                        throw new BusinessRuleViolationException("WINNER_REQUIRED",
                                "Nobody bid: choose who collects in this turn");
                    }
                    return finish(auction, cycle, access.period(), drawAmong(cycle), AuctionResolution.DRAW);
                }));
    }

    @Override
    public Result<Integer, ApplicationError> handle(CloseDueAuctionsCommand command) {
        var closed = 0;
        var today = today();
        for (var auction : auctionRepository.findAllOpen()) {
            if (!auction.isDue(today)) {
                continue;
            }
            var cycle = cycleRepository.findById(auction.getCycleId()).orElseThrow();
            var period = periodRepository.findById(auction.getPeriodId()).orElseThrow();
            if (auction.highestBid().isPresent()) {
                finish(auction, cycle, period, auction.highestBid().get().membershipId(), AuctionResolution.BID);
                closed++;
            } else if (auction.getFallback() == AuctionFallback.DRAW) {
                finish(auction, cycle, period, drawAmong(cycle), AuctionResolution.DRAW);
                closed++;
            }
        }
        return Result.success(closed);
    }

    /**
     * Gives the turn in progress to the winner, records what they collect and closes the auction. The members who
     * still have not collected, other than the winner, share the discount.
     */
    private AuctionView finish(Auction auction, Cycle cycle, Period period, UUID winnerMembershipId,
                               AuctionResolution resolution) {
        var stillToCollect = cycle.stillToCollect().size();
        cycle.giveCurrentTurnTo(winnerMembershipId);
        auction.close(winnerMembershipId, resolution, stillToCollect - 1, clock.instant());
        period.applyAuctionResult(winnerMembershipId, auction.getDiscount());
        var savedCycle = cycleRepository.save(cycle);
        var savedPeriod = periodRepository.save(period);
        return new AuctionView(auctionRepository.save(auction), savedCycle, savedPeriod);
    }

    private UUID drawAmong(Cycle cycle) {
        var candidates = cycle.stillToCollect();
        return candidates.get(random.nextInt(candidates.size())).membershipId();
    }

    private Result<Auction, ApplicationError> auctionOf(Period period) {
        return auctionRepository.findByPeriodId(period.getId())
                .<Result<Auction, ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(ApplicationError.notFound("Auction", period.getId().toString())));
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(GROUP_ZONE));
    }
}
