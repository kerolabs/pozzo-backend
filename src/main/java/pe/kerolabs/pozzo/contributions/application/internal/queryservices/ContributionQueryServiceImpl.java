package pe.kerolabs.pozzo.contributions.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.contributions.application.queryservices.ContributionQueryService;
import pe.kerolabs.pozzo.contributions.application.queryservices.PeriodView;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetCurrentPeriodQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetCycleByGroupIdQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetCycleByIdQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetMemberContributionsQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetPendingReviewsQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetReceiptImageQuery;
import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageLink;
import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageStorage;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetPeriodsQuery;
import pe.kerolabs.pozzo.contributions.domain.repositories.ContributionRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.CycleRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.PeriodRepository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves the read side of Contributions, only for the members of each cycle.
 */
@Service
@Transactional(readOnly = true)
public class ContributionQueryServiceImpl implements ContributionQueryService {

    /** Long enough to open the image, short enough that a copied link soon stops working. */
    private static final Duration RECEIPT_LINK_VALIDITY = Duration.ofMinutes(15);

    private final CycleRepository cycleRepository;
    private final PeriodRepository periodRepository;
    private final ContributionRepository contributionRepository;
    private final ReceiptImageStorage receiptImageStorage;

    public ContributionQueryServiceImpl(CycleRepository cycleRepository, PeriodRepository periodRepository,
                                        ContributionRepository contributionRepository,
                                        ReceiptImageStorage receiptImageStorage) {
        this.cycleRepository = cycleRepository;
        this.periodRepository = periodRepository;
        this.contributionRepository = contributionRepository;
        this.receiptImageStorage = receiptImageStorage;
    }

    @Override
    public Optional<Cycle> handle(GetCycleByGroupIdQuery query) {
        return cycleRepository.findByGroupId(query.groupId())
                .filter(cycle -> cycle.isParticipant(query.requesterAccountId()));
    }

    @Override
    public Optional<Cycle> handle(GetCycleByIdQuery query) {
        return findForParticipant(query.cycleId(), query.requesterAccountId());
    }

    @Override
    public Optional<PeriodView> handle(GetCurrentPeriodQuery query) {
        return findForParticipant(query.cycleId(), query.requesterAccountId())
                .flatMap(cycle -> periodRepository.findByCycleIdAndTurnNumber(cycle.getId(), cycle.getCurrentTurn())
                        .map(period -> new PeriodView(cycle, period,
                                contributionRepository.findAllByPeriodId(period.getId()))));
    }

    @Override
    public Optional<List<PeriodView>> handle(GetPeriodsQuery query) {
        return findForParticipant(query.cycleId(), query.requesterAccountId())
                .map(cycle -> periodRepository.findAllByCycleId(cycle.getId()).stream()
                        .map(period -> new PeriodView(cycle, period,
                                contributionRepository.findAllByPeriodId(period.getId())))
                        .toList());
    }

    @Override
    public Optional<List<PeriodView>> handle(GetMemberContributionsQuery query) {
        return findForParticipant(query.cycleId(), query.requesterAccountId())
                .map(cycle -> {
                    var membershipId = cycle.participantOf(query.requesterAccountId()).orElseThrow().membershipId();
                    var mine = contributionRepository.findAllByCycleIdAndMembershipId(cycle.getId(), membershipId);
                    return periodRepository.findAllByCycleId(cycle.getId()).stream()
                            .map(period -> new PeriodView(cycle, period, mine.stream()
                                    .filter(contribution -> contribution.getPeriodId().equals(period.getId()))
                                    .toList()))
                            .toList();
                });
    }

    @Override
    public Optional<PeriodView> handle(GetPendingReviewsQuery query) {
        return periodRepository.findById(query.periodId())
                .flatMap(period -> findForParticipant(period.getCycleId(), query.requesterAccountId())
                        .filter(cycle -> cycle.isOrganizer(query.requesterAccountId()))
                        .map(cycle -> new PeriodView(cycle, period,
                                contributionRepository.findAllByPeriodId(period.getId()).stream()
                                        .filter(Contribution::isUnderReview)
                                        .toList())));
    }

    @Override
    public Optional<ReceiptImageLink> handle(GetReceiptImageQuery query) {
        var requester = query.requesterAccountId();
        return contributionRepository.findById(query.contributionId())
                .filter(Contribution::hasReceiptImage)
                .filter(contribution -> requester.equals(contribution.getAccountId())
                        || findForParticipant(contribution.getCycleId(), requester)
                        .filter(cycle -> cycle.isOrganizer(requester))
                        .isPresent())
                .map(contribution -> receiptImageStorage.link(contribution.getReceiptImagePath(), RECEIPT_LINK_VALIDITY));
    }

    private Optional<Cycle> findForParticipant(UUID cycleId, UUID requesterAccountId) {
        return cycleRepository.findById(cycleId).filter(cycle -> cycle.isParticipant(requesterAccountId));
    }
}
