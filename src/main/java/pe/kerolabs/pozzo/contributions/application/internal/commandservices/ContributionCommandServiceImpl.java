package pe.kerolabs.pozzo.contributions.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.contributions.application.commandservices.ContributionCommandService;
import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageStorage;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.model.commands.AttachReceiptImageCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterCashContributionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterContributionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterCoverageCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.ReviewContributionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ExpectedStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.PaymentReceipt;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReviewDecision;
import pe.kerolabs.pozzo.contributions.domain.repositories.ContributionRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.CycleRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.PeriodRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Registers contributions (with a receipt, in cash or as a coverage), settles what each member owes
 * in the period and handles the organizer's review of the receipts that did not match.
 */
@Service
@Transactional
public class ContributionCommandServiceImpl implements ContributionCommandService {

    static final int MAX_RECEIPT_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> RECEIPT_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final CycleRepository cycleRepository;
    private final PeriodRepository periodRepository;
    private final ContributionRepository contributionRepository;
    private final ReceiptImageStorage receiptImageStorage;
    private final Clock clock;

    public ContributionCommandServiceImpl(CycleRepository cycleRepository, PeriodRepository periodRepository,
                                          ContributionRepository contributionRepository,
                                          ReceiptImageStorage receiptImageStorage, Clock clock) {
        this.cycleRepository = cycleRepository;
        this.periodRepository = periodRepository;
        this.contributionRepository = contributionRepository;
        this.receiptImageStorage = receiptImageStorage;
        this.clock = clock;
    }

    @Override
    public Result<Contribution, ApplicationError> handle(RegisterContributionCommand command) {
        var now = clock.instant();
        return CycleAccess.requireParticipant(cycleRepository, periodRepository, command.periodId(),
                        command.requesterAccountId())
                .flatMap(access -> {
                    var cycle = access.cycle();
                    var period = access.period();
                    var member = cycle.participantOf(command.requesterAccountId()).orElseThrow();
                    var blocked = checkCanContribute(period, member.membershipId());
                    if (blocked != null) {
                        return Result.failure(blocked);
                    }
                    if (contributionRepository.existsByCycleIdAndOperationNumber(cycle.getId(),
                            command.operationNumber().strip())) {
                        return Result.failure(ApplicationError.conflict(
                                "Receipt", "This receipt was already used in the group"));
                    }
                    var currency = cycle.getRules().contribution().currency();
                    var receipt = new PaymentReceipt(command.operationNumber(), command.payerName(),
                            command.payeeName(), Money.of(command.amount(), currency), command.paidAt(),
                            command.source());
                    var contribution = Contribution.fromReceipt(cycle.getId(), period.getId(), member.membershipId(),
                            command.requesterAccountId(), receipt, expectedAmount(period, member.membershipId()),
                            period.getCutoffDate(), cycle.getRules().payeeName(), now);
                    return Result.success(saveAndSettle(contribution, period, ExpectedStatus.PAID, now));
                });
    }

    @Override
    public Result<Contribution, ApplicationError> handle(RegisterCashContributionCommand command) {
        var now = clock.instant();
        return CycleAccess.requireOrganizer(cycleRepository, periodRepository, command.periodId(),
                        command.requesterAccountId())
                .flatMap(access -> {
                    var cycle = access.cycle();
                    var period = access.period();
                    var member = cycle.findParticipant(command.membershipId());
                    if (member.isEmpty()) {
                        return Result.failure(ApplicationError.notFound("Membership", command.membershipId().toString()));
                    }
                    var blocked = checkCanContribute(period, command.membershipId());
                    if (blocked != null) {
                        return Result.failure(blocked);
                    }
                    var expected = expectedAmount(period, command.membershipId());
                    var amount = Money.of(command.amount(), expected.currency());
                    if (!amount.isSameAmountAs(expected)) {
                        return Result.failure(ApplicationError.businessRuleViolation(
                                "AMOUNT_MISMATCH", "The cash amount must be the contribution of %s".formatted(expected)));
                    }
                    var contribution = Contribution.inCash(cycle.getId(), period.getId(), command.membershipId(),
                            member.get().accountId(), amount, command.receivedOn(), period.getCutoffDate(),
                            command.requesterAccountId(), now);
                    return Result.success(saveAndSettle(contribution, period, ExpectedStatus.PAID, now));
                });
    }

    @Override
    public Result<Contribution, ApplicationError> handle(RegisterCoverageCommand command) {
        var now = clock.instant();
        return CycleAccess.requireOrganizer(cycleRepository, periodRepository, command.periodId(),
                        command.requesterAccountId())
                .flatMap(access -> {
                    var cycle = access.cycle();
                    var period = access.period();
                    var member = cycle.findParticipant(command.membershipId());
                    if (member.isEmpty() || cycle.findParticipant(command.coveredByMembershipId()).isEmpty()) {
                        return Result.failure(ApplicationError.notFound("Membership", command.membershipId().toString()));
                    }
                    var blocked = checkCanContribute(period, command.membershipId());
                    if (blocked != null) {
                        return Result.failure(blocked);
                    }
                    var contribution = Contribution.asCoverage(cycle.getId(), period.getId(), command.membershipId(),
                            member.get().accountId(), command.coveredByMembershipId(),
                            expectedAmount(period, command.membershipId()), command.requesterAccountId(), now);
                    return Result.success(saveAndSettle(contribution, period, ExpectedStatus.COVERED, now));
                });
    }

    @Override
    public Result<Contribution, ApplicationError> handle(ReviewContributionCommand command) {
        var now = clock.instant();
        var contribution = contributionRepository.findById(command.contributionId());
        if (contribution.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Contribution", command.contributionId().toString()));
        }
        return CycleAccess.requireOrganizer(cycleRepository, periodRepository, contribution.get().getPeriodId(),
                        command.requesterAccountId())
                .mapError(error -> error.code().equals("PERIOD_NOT_FOUND")
                        ? ApplicationError.notFound("Contribution", command.contributionId().toString())
                        : error)
                .map(access -> {
                    var reviewed = contribution.get();
                    if (command.decision() == ReviewDecision.APPROVE) {
                        reviewed.approve(command.requesterAccountId(), command.note(), access.period().getCutoffDate(), now);
                        return saveAndSettle(reviewed, access.period(), ExpectedStatus.PAID, now);
                    }
                    reviewed.reject(command.requesterAccountId(), command.note(), now);
                    return contributionRepository.save(reviewed);
                });
    }

    @Override
    public Result<Contribution, ApplicationError> handle(AttachReceiptImageCommand command) {
        if (command.contentType() == null || !RECEIPT_IMAGE_TYPES.contains(command.contentType())) {
            return Result.failure(ApplicationError.validationError("image", "must be a JPEG, PNG or WebP image"));
        }
        if (command.content().length == 0 || command.content().length > MAX_RECEIPT_IMAGE_BYTES) {
            return Result.failure(ApplicationError.validationError("image", "must weigh up to 2 MB"));
        }
        // Only the member who registered the contribution; anyone else gets the same answer as for a missing one.
        var contribution = contributionRepository.findById(command.contributionId())
                .filter(found -> command.requesterAccountId().equals(found.getRegisteredByAccountId()));
        if (contribution.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Contribution", command.contributionId().toString()));
        }
        var path = receiptImageStorage.store(command.contributionId(), command.content(), command.contentType());
        contribution.get().attachReceiptImage(path);
        return Result.success(contributionRepository.save(contribution.get()));
    }

    /**
     * Saves the contribution and, when it is valid, settles what the member owed in the period.
     */
    private Contribution saveAndSettle(Contribution contribution, Period period, ExpectedStatus how, Instant now) {
        var saved = contributionRepository.save(contribution);
        if (saved.isValid()) {
            period.settle(saved.getMembershipId(), how, saved.getId(), now);
            periodRepository.save(period);
        }
        return saved;
    }

    private ApplicationError checkCanContribute(Period period, UUID membershipId) {
        if (!period.isOpen()) {
            return ApplicationError.businessRuleViolation("PERIOD_NOT_OPEN", "The period no longer accepts contributions");
        }
        var expected = period.expectedFor(membershipId);
        if (expected.isEmpty() || !expected.get().isPending()) {
            return ApplicationError.businessRuleViolation(
                    "CONTRIBUTION_ALREADY_SETTLED", "The member already settled this period");
        }
        var underReview = contributionRepository.findAllByPeriodId(period.getId()).stream()
                .anyMatch(contribution -> contribution.getMembershipId().equals(membershipId)
                        && contribution.isUnderReview());
        if (underReview) {
            return ApplicationError.businessRuleViolation(
                    "CONTRIBUTION_UNDER_REVIEW", "A contribution of the member is waiting for the organizer's review");
        }
        return null;
    }

    private static Money expectedAmount(Period period, UUID membershipId) {
        return period.expectedFor(membershipId).orElseThrow().getAmount();
    }
}
