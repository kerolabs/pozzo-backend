package pe.kerolabs.pozzo.compliancehistory.application.commandservices;

import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.ShareLink;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.RecordComplianceEntryCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.RevokeShareLinkCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.ShareHistoryCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for writing the compliance history; the only one that writes.
 */
public interface ComplianceCommandService {

    /**
     * Records a fact, creating the member's record if needed. A fact already recorded is ignored.
     */
    Result<MemberRecord, ApplicationError> handle(RecordComplianceEntryCommand command);

    Result<ShareLink, ApplicationError> handle(ShareHistoryCommand command);

    Result<ShareLink, ApplicationError> handle(RevokeShareLinkCommand command);
}
