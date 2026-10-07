package pe.kerolabs.pozzo.compliancehistory.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.compliancehistory.application.commandservices.ComplianceCommandService;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.ShareLink;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.RecordComplianceEntryCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.RevokeShareLinkCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.ShareHistoryCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ShareToken;
import pe.kerolabs.pozzo.compliancehistory.domain.repositories.MemberRecordRepository;
import pe.kerolabs.pozzo.compliancehistory.domain.repositories.ShareLinkRepository;
import pe.kerolabs.pozzo.compliancehistory.domain.services.ComplianceScoringService;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;

/**
 * Records compliance facts and manages share links.
 */
@Service
@Transactional
public class ComplianceCommandServiceImpl implements ComplianceCommandService {

    private final MemberRecordRepository memberRecordRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final ComplianceScoringService scoringService;
    private final Clock clock;

    public ComplianceCommandServiceImpl(MemberRecordRepository memberRecordRepository,
                                        ShareLinkRepository shareLinkRepository,
                                        ComplianceScoringService scoringService,
                                        Clock clock) {
        this.memberRecordRepository = memberRecordRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.scoringService = scoringService;
        this.clock = clock;
    }

    @Override
    public Result<MemberRecord, ApplicationError> handle(RecordComplianceEntryCommand command) {
        var now = clock.instant();
        var record = memberRecordRepository.findByAccountId(command.accountId())
                .orElseGet(() -> MemberRecord.forMember(command.accountId(), now));
        if (memberRecordRepository.existsEntryBySourceEventId(command.sourceEventId())) {
            return Result.success(record);
        }
        record.record(ComplianceEntry.of(command.cycleId(), command.groupId(), command.groupName(), command.periodId(),
                command.kind(), command.occurredAt(), command.sourceEventId()), scoringService, now);
        return Result.success(memberRecordRepository.save(record));
    }

    @Override
    public Result<ShareLink, ApplicationError> handle(ShareHistoryCommand command) {
        return Result.success(shareLinkRepository.save(ShareLink.issue(command.accountId(), clock.instant())));
    }

    @Override
    public Result<ShareLink, ApplicationError> handle(RevokeShareLinkCommand command) {
        ShareToken token;
        try {
            token = new ShareToken(command.token());
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.notFound("ShareLink", command.token()));
        }
        return shareLinkRepository.findByToken(token)
                .filter(link -> link.getAccountId().equals(command.accountId()))
                .<Result<ShareLink, ApplicationError>>map(link -> {
                    link.revoke();
                    return Result.success(shareLinkRepository.save(link));
                })
                .orElseGet(() -> Result.failure(ApplicationError.notFound("ShareLink", command.token())));
    }
}
