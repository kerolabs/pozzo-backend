package pe.kerolabs.pozzo.compliancehistory.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.compliancehistory.application.commandservices.ComplianceCommandService;
import pe.kerolabs.pozzo.compliancehistory.application.queryservices.ComplianceQueryService;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.RevokeShareLinkCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.ShareHistoryCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetGroupComplianceQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetMemberSummaryQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetMyHistoryQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetSharedHistoryQuery;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.links.SharedHistoryLinkBuilder;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.ComplianceSummaryResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.MemberComplianceResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.MyHistoryResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.ShareLinkResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.SharedHistoryResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.transform.ComplianceResourceAssembler;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.UUID;

/**
 * Reading and sharing the compliance history. The member sees the detail; organizers and shared
 * links only get the summary.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Compliance History", description = "Compliance history of the members")
public class ComplianceHistoryController {

    private final ComplianceCommandService complianceCommandService;
    private final ComplianceQueryService complianceQueryService;
    private final SharedHistoryLinkBuilder linkBuilder;

    public ComplianceHistoryController(ComplianceCommandService complianceCommandService,
                                       ComplianceQueryService complianceQueryService,
                                       SharedHistoryLinkBuilder linkBuilder) {
        this.complianceCommandService = complianceCommandService;
        this.complianceQueryService = complianceQueryService;
        this.linkBuilder = linkBuilder;
    }

    /**
     * Retrieves the compliance history for the authenticated member across all groups.
     *
     * @param member the authenticated member making the request
     * @return the aggregated compliance history summary and per-group breakdown
     */
    @GetMapping("/members/me/compliance")
    @Operation(summary = "Get my history", description = "Summary across every group and the detail by group.")
    @ApiResponse(responseCode = "200", description = "My history",
            content = @Content(schema = @Schema(implementation = MyHistoryResource.class)))
    public ResponseEntity<MyHistoryResource> getMyHistory(@AuthenticationPrincipal AuthenticatedMember member) {
        return ResponseEntity.ok(ComplianceResourceAssembler.toResourceFromRecord(
                complianceQueryService.handle(new GetMyHistoryQuery(member.accountId()))));
    }

    /**
     * Retrieves the compliance records of all members in a specific savings group.
     * Only accessible by the group organizer.
     *
     * @param member the authenticated member requesting the compliance list (must be organizer)
     * @param groupId the unique identifier of the savings group
     * @return list of compliance details for each group member, or 404 if not found/unauthorized
     */
    @GetMapping("/groups/{groupId}/compliance")
    @Operation(summary = "Get the compliance of the members of a group", description = "Only the organizer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compliance of every member",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberComplianceResource.class)))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not its organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getGroupCompliance(@AuthenticationPrincipal AuthenticatedMember member,
                                                @PathVariable UUID groupId) {
        return complianceQueryService.handle(new GetGroupComplianceQuery(groupId, member.accountId()))
                .<ResponseEntity<?>>map(members -> ResponseEntity.ok(members.stream()
                        .map(ComplianceResourceAssembler::toResourceFromMemberCompliance)
                        .toList()))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("SavingsGroup", groupId.toString())));
    }

    /**
     * Retrieves the high-level compliance summary score of a specific member.
     * Accessible by the member themselves or organizers of mutual savings groups.
     *
     * @param member the authenticated member making the request
     * @param memberId the identifier of the member whose summary is requested
     * @return the compliance score summary or 404 if not accessible
     */
    @GetMapping("/members/{memberId}/compliance/summary")
    @Operation(summary = "Get the summary of a member",
            description = "Only the member, or an organizer of a group the member belongs to.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary",
                    content = @Content(schema = @Schema(implementation = ComplianceSummaryResource.class))),
            @ApiResponse(responseCode = "404", description = "Not available to the requester",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getMemberSummary(@AuthenticationPrincipal AuthenticatedMember member,
                                              @PathVariable UUID memberId) {
        return complianceQueryService.handle(new GetMemberSummaryQuery(memberId, member.accountId()))
                .<ResponseEntity<?>>map(summary -> ResponseEntity.ok(
                        ComplianceResourceAssembler.toResourceFromSummary(summary)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Member", memberId.toString())));
    }

    /**
     * Creates a publicly shareable, tokenized link to the authenticated member's compliance summary.
     * Links remain valid for 7 days unless manually revoked.
     *
     * @param member the authenticated member generating the share link
     * @return the created share link resource containing the access token and expiration
     */
    @PostMapping("/members/me/compliance/share")
    @Operation(summary = "Share my history", description = "Creates a public link to the summary, valid for 7 days.")
    @ApiResponse(responseCode = "201", description = "Link created",
            content = @Content(schema = @Schema(implementation = ShareLinkResource.class)))
    public ResponseEntity<?> shareHistory(@AuthenticationPrincipal AuthenticatedMember member) {
        var result = complianceCommandService.handle(new ShareHistoryCommand(member.accountId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                link -> new ShareLinkResource(link.getToken().value(), linkBuilder.linkFor(link.getToken()),
                        link.getExpiresAt()),
                HttpStatus.CREATED);
    }

    /**
     * Revokes an existing public share link created by the authenticated member.
     *
     * @param member the authenticated member who owns the share link
     * @param token the public share link token to revoke
     * @return 204 No Content on success, or 404 if not found or unauthorized
     */
    @DeleteMapping("/compliance/shares/{token}")
    @Operation(summary = "Revoke a share link", description = "Only the member who created it.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Link revoked", content = @Content),
            @ApiResponse(responseCode = "404", description = "The link does not exist or is not the requester's",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> revokeShareLink(@AuthenticationPrincipal AuthenticatedMember member,
                                             @PathVariable String token) {
        return ResponseEntityAssembler.toNoContentResponseEntityFromResult(
                complianceCommandService.handle(new RevokeShareLinkCommand(token, member.accountId())));
    }

    /**
     * Public endpoint to view a member's compliance summary via a valid, unexpired share token.
     *
     * @param token the unique share link token
     * @return the shared compliance summary, or 404 if token expired, revoked, or non-existent
     */
    @GetMapping("/compliance/shared/{token}")
    @SecurityRequirements
    @Operation(summary = "Open a shared history", description = "Public: shows the summary behind a valid link.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shared summary",
                    content = @Content(schema = @Schema(implementation = SharedHistoryResource.class))),
            @ApiResponse(responseCode = "404", description = "The link does not exist, expired or was revoked",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getSharedHistory(@PathVariable String token) {
        return complianceQueryService.handle(new GetSharedHistoryQuery(token))
                .<ResponseEntity<?>>map(shared -> ResponseEntity.ok(
                        ComplianceResourceAssembler.toResourceFromSharedHistory(shared)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("ShareLink", token)));
    }
}
