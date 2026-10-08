package pe.kerolabs.pozzo.compliancehistory.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.compliancehistory.application.internal.outboundservices.acl.ExternalContextsService;
import pe.kerolabs.pozzo.compliancehistory.application.queryservices.ComplianceQueryService;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetGroupComplianceQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetMemberSummaryQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetMyHistoryQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetSharedHistoryQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ShareToken;
import pe.kerolabs.pozzo.compliancehistory.domain.repositories.MemberRecordRepository;
import pe.kerolabs.pozzo.compliancehistory.domain.repositories.ShareLinkRepository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves the compliance history queries.
 */
@Service
@Transactional(readOnly = true)
public class ComplianceQueryServiceImpl implements ComplianceQueryService {

    private final MemberRecordRepository memberRecordRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final ExternalContextsService externalContextsService;
    private final Clock clock;

    public ComplianceQueryServiceImpl(MemberRecordRepository memberRecordRepository,
                                      ShareLinkRepository shareLinkRepository,
                                      ExternalContextsService externalContextsService,
                                      Clock clock) {
        this.memberRecordRepository = memberRecordRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.externalContextsService = externalContextsService;
        this.clock = clock;
    }

    @Override
    public MemberRecord handle(GetMyHistoryQuery query) {
        return memberRecordRepository.findByAccountId(query.accountId())
                .orElseGet(() -> MemberRecord.forMember(query.accountId(), clock.instant()));
    }

    @Override
    public Optional<List<MemberCompliance>> handle(GetGroupComplianceQuery query) {
        if (!externalContextsService.isOrganizer(query.groupId(), query.requesterAccountId())) {
            return Optional.empty();
        }
        return Optional.of(externalContextsService.fetchActiveMembers(query.groupId()).stream()
                .map(member -> new MemberCompliance(member.membershipId(), member.accountId(), member.displayName(),
                        member.organizer(), member.accountId() == null ? null : summaryOf(member.accountId())))
                .toList());
    }

    @Override
    public Optional<ComplianceSummary> handle(GetMemberSummaryQuery query) {
        var allowed = query.memberAccountId().equals(query.requesterAccountId())
                || externalContextsService.isOrganizerOfMember(query.requesterAccountId(), query.memberAccountId());
        return allowed ? Optional.of(summaryOf(query.memberAccountId())) : Optional.empty();
    }

    @Override
    public Optional<SharedHistory> handle(GetSharedHistoryQuery query) {
        var now = clock.instant();
        ShareToken token;
        try {
            token = new ShareToken(query.token());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        return shareLinkRepository.findByToken(token)
                .filter(link -> link.isValid(now))
                .map(link -> new SharedHistory(
                        externalContextsService.fetchDisplayName(link.getAccountId()).orElse(""),
                        summaryOf(link.getAccountId()),
                        link.getExpiresAt()));
    }

    private ComplianceSummary summaryOf(UUID accountId) {
        return memberRecordRepository.findByAccountId(accountId)
                .map(MemberRecord::getSummary)
                .orElseGet(ComplianceSummary::empty);
    }
}
