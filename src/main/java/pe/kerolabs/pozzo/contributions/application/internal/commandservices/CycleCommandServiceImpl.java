package pe.kerolabs.pozzo.contributions.application.internal.commandservices;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.contributions.application.commandservices.CycleCommandService;
import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageRules;
import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageStorage;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Auction;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.model.commands.AttachPayoutProofCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.DeliverPotCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.LinkCycleMemberCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterCycleDropoutCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.ReplaceCycleMemberCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.StartCycleCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.SwapTurnsCommand;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleRules;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.repositories.AuctionRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.CycleRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.PeriodRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Starts cycles and moves them from one turn to the next as each pot is delivered.
 */
@Service
@Transactional
public class CycleCommandServiceImpl implements CycleCommandService {

    private final CycleRepository cycleRepository;
    private final PeriodRepository periodRepository;
    private final ReceiptImageStorage receiptImageStorage;
    private final AuctionRepository auctionRepository;
    private final Clock clock;

    public CycleCommandServiceImpl(CycleRepository cycleRepository, PeriodRepository periodRepository,
                                   ReceiptImageStorage receiptImageStorage, AuctionRepository auctionRepository,
                                   Clock clock) {
        this.cycleRepository = cycleRepository;
        this.periodRepository = periodRepository;
        this.receiptImageStorage = receiptImageStorage;
        this.auctionRepository = auctionRepository;
        this.clock = clock;
    }

    @Override
    public Result<Cycle, ApplicationError> handle(StartCycleCommand command) {
        var existing = cycleRepository.findByGroupId(command.groupId());
        if (existing.isPresent()) {
            return Result.success(existing.get());
        }
        var now = clock.instant();
        var rules = new CycleRules(Money.of(command.contributionAmount(), command.currency()), command.periodicity(),
                command.firstContributionDate(), command.destinationMethod(), command.destinationPhone(),
                command.organizerName());
        var cycle = Cycle.start(command.groupId(), command.groupName(), command.organizerAccountId(), rules,
                command.turns(), command.auction(), now);
        var saved = cycleRepository.save(cycle);
        openAuctionIfDue(saved, periodRepository.save(Period.open(saved, 1, now)), now);
        return Result.success(saved);
    }

    /**
     * Opens the auction of a period of an auction cycle while more than one member has not collected; the last
     * one collects the full pot without an auction.
     */
    private void openAuctionIfDue(Cycle cycle, Period period, Instant now) {
        if (cycle.isAuction() && cycle.stillToCollect().size() > 1) {
            auctionRepository.save(Auction.open(cycle, period, now));
        }
    }

    /**
     * The credit each member who still has not collected earned from the discount of the auction just closed.
     */
    private static Map<UUID, Money> creditsAfter(@Nullable Auction auction, Cycle cycle) {
        if (auction == null || auction.getCreditPerMember() == null || auction.getCreditPerMember().amount().signum() == 0) {
            return Map.of();
        }
        var credits = new HashMap<UUID, Money>();
        cycle.stillToCollect().forEach(turn -> credits.put(turn.membershipId(), auction.getCreditPerMember()));
        return credits;
    }

    @Override
    public Result<PotDelivery, ApplicationError> handle(DeliverPotCommand command) {
        var now = clock.instant();
        return CycleAccess.requireOrganizer(cycleRepository, periodRepository, command.periodId(),
                        command.requesterAccountId())
                .map(access -> {
                    var cycle = access.cycle();
                    var period = access.period();
                    var auction = auctionRepository.findByPeriodId(period.getId());
                    if (auction.filter(Auction::isOpen).isPresent()) {
                        throw new BusinessRuleViolationException("AUCTION_STILL_OPEN",
                                "The auction of this turn must close before delivering the pot");
                    }
                    period.deliverPot(command.requesterAccountId(), now);
                    var delivered = periodRepository.save(period);
                    Period next = null;
                    if (cycle.hasRemainingTurns()) {
                        var nextTurn = cycle.advanceToNextTurn();
                        next = Period.open(cycle, nextTurn, creditsAfter(auction.orElse(null), cycle), now);
                    } else {
                        cycle.close(now);
                    }
                    var savedCycle = cycleRepository.save(cycle);
                    var savedNext = next == null ? null : periodRepository.save(next);
                    if (savedNext != null) {
                        openAuctionIfDue(savedCycle, savedNext, now);
                    }
                    return new PotDelivery(savedCycle, delivered, savedNext);
                });
    }

    @Override
    public Result<Boolean, ApplicationError> handle(LinkCycleMemberCommand command) {
        var cycle = cycleRepository.findByGroupId(command.groupId());
        if (cycle.isEmpty() || !cycle.get().linkAccount(command.membershipId(), command.accountId(), clock.instant())) {
            return Result.success(false);
        }
        cycleRepository.save(cycle.get());
        return Result.success(true);
    }

    @Override
    public Result<Period, ApplicationError> handle(AttachPayoutProofCommand command) {
        var invalid = ReceiptImageRules.check(command.content(), command.contentType());
        if (invalid.isPresent()) {
            return Result.failure(invalid.get());
        }
        return CycleAccess.requireOrganizer(cycleRepository, periodRepository, command.periodId(),
                        command.requesterAccountId())
                .flatMap(access -> {
                    var period = access.period();
                    // Checked before storing, so no image is uploaded for a pot that was not delivered.
                    if (!period.isDelivered()) {
                        return Result.failure(ApplicationError.businessRuleViolation("POT_NOT_DELIVERED",
                                "The proof can only be attached once the pot was delivered"));
                    }
                    period.attachPayoutProof(receiptImageStorage.store(period.getId(), command.content(),
                            command.contentType()));
                    return Result.success(periodRepository.save(period));
                });
    }

    @Override
    public Result<Cycle, ApplicationError> handle(SwapTurnsCommand command) {
        var cycle = cycleRepository.findById(command.cycleId())
                .filter(found -> found.isParticipant(command.requesterAccountId()));
        if (cycle.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Cycle", command.cycleId().toString()));
        }
        if (!cycle.get().isOrganizer(command.requesterAccountId())) {
            return Result.failure(ApplicationError.forbidden(
                    "ONLY_ORGANIZER", "Only the organizer of the group can do this"));
        }
        cycle.get().swapTurns(command.firstMembershipId(), command.secondMembershipId(), clock.instant());
        return Result.success(cycleRepository.save(cycle.get()));
    }

    @Override
    public Result<Cycle, ApplicationError> handle(RegisterCycleDropoutCommand command) {
        var cycle = cycleRepository.findByGroupId(command.groupId());
        if (cycle.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Cycle", command.groupId().toString()));
        }
        cycle.get().registerDropout(command.membershipId(), clock.instant());
        return Result.success(cycleRepository.save(cycle.get()));
    }

    @Override
    public Result<Cycle, ApplicationError> handle(ReplaceCycleMemberCommand command) {
        var cycle = cycleRepository.findByGroupId(command.groupId());
        if (cycle.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Cycle", command.groupId().toString()));
        }
        var current = periodRepository.findByCycleIdAndTurnNumber(cycle.get().getId(), cycle.get().getCurrentTurn());
        cycle.get().replaceMember(command.droppedMembershipId(), command.replacementMembershipId(),
                command.accountId(), command.displayName(), current.map(Period::isDelivered).orElse(false));
        current.filter(period -> !period.isDelivered()).ifPresent(period -> {
            period.reassignMember(command.droppedMembershipId(), command.replacementMembershipId());
            periodRepository.save(period);
        });
        return Result.success(cycleRepository.save(cycle.get()));
    }
}
