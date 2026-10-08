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
     * Records a compliance entry fact (e.g. payment on-time, late, covered, or defaulted),
     * creating the member's aggregate record if needed. Duplicate facts are idempotent.
     *
     * @param command command containing member ID, group ID, period ID, and entry kind
     * @return the updated member record, or an error if invalid
     */
    Result<MemberRecord, ApplicationError> handle(RecordComplianceEntryCommand command);

    /**
     * Creates a public 7-day share link for the member's compliance summary.
     *
     * @param command command with the member's account identifier
     * @return the created share link aggregate with token and expiration, or an error
     */
    Result<ShareLink, ApplicationError> handle(ShareHistoryCommand command);

    /**
     * Revokes an existing public share link created by the member.
     *
     * @param command command with share token and member account identifier
     * @return the revoked share link aggregate, or an error if not found/unauthorized
     */
    Result<ShareLink, ApplicationError> handle(RevokeShareLinkCommand command);
}
