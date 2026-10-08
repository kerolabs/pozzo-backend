package pe.kerolabs.pozzo.savingsgroups.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.savingsgroups.application.commandservices.SavingsGroupCommandService;
import pe.kerolabs.pozzo.savingsgroups.application.internal.outboundservices.acl.ExternalIamService;
import pe.kerolabs.pozzo.savingsgroups.application.queryservices.SavingsGroupQueryService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.GenerateInvitationCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.JoinGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.RemoveMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetActiveInvitationQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetGroupPreviewQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetMembersQuery;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.links.InvitationLinkBuilder;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.AddManualMemberResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.GroupPreviewResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.GroupResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.InvitationResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.MembershipResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform.GroupCommandFromResourceAssembler;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform.GroupPreviewResourceFromEntityAssembler;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform.GroupResourceFromEntityAssembler;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform.InvitationResourceFromEntityAssembler;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform.MembershipResourceFromEntityAssembler;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.UUID;

/**
 * Invitations and members: invite with a code, preview and join a group, list the members,
 * register members without the application and remove members before the group starts.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Memberships", description = "Invitations and members of a savings group")
public class MembershipsController {

    private final SavingsGroupCommandService savingsGroupCommandService;
    private final SavingsGroupQueryService savingsGroupQueryService;
    private final InvitationLinkBuilder invitationLinkBuilder;
    private final ExternalIamService externalIamService;

    public MembershipsController(SavingsGroupCommandService savingsGroupCommandService,
                                 SavingsGroupQueryService savingsGroupQueryService,
                                 InvitationLinkBuilder invitationLinkBuilder,
                                 ExternalIamService externalIamService) {
        this.savingsGroupCommandService = savingsGroupCommandService;
        this.savingsGroupQueryService = savingsGroupQueryService;
        this.invitationLinkBuilder = invitationLinkBuilder;
        this.externalIamService = externalIamService;
    }

    @PostMapping("/groups/{groupId}/invitations")
    @Operation(summary = "Generate an invitation", description = "Only the organizer. The previous invitation expires.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invitation generated",
                    content = @Content(schema = @Schema(implementation = InvitationResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The group already started",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> generateInvitation(@AuthenticationPrincipal AuthenticatedMember member,
                                                @PathVariable UUID groupId) {
        var result = savingsGroupCommandService.handle(new GenerateInvitationCommand(groupId, member.accountId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                invitation -> InvitationResourceFromEntityAssembler.toResourceFromEntity(
                        invitation, invitationLinkBuilder.linkFor(invitation.getCode())),
                HttpStatus.CREATED);
    }

    @GetMapping("/groups/{groupId}/invitations/active")
    @Operation(summary = "Get the active invitation", description = "Only the organizer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active invitation",
                    content = @Content(schema = @Schema(implementation = InvitationResource.class))),
            @ApiResponse(responseCode = "404", description = "No active invitation, or the requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getActiveInvitation(@AuthenticationPrincipal AuthenticatedMember member,
                                                 @PathVariable UUID groupId) {
        return savingsGroupQueryService.handle(new GetActiveInvitationQuery(groupId, member.accountId()))
                .<ResponseEntity<?>>map(invitation -> ResponseEntity.ok(
                        InvitationResourceFromEntityAssembler.toResourceFromEntity(
                                invitation, invitationLinkBuilder.linkFor(invitation.getCode()))))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Invitation", groupId.toString())));
    }

    @GetMapping("/invitations/{code}")
    @Operation(summary = "Preview a group before joining",
            description = "Shows the name, organizer, rules and free seats, without the member list.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group preview",
                    content = @Content(schema = @Schema(implementation = GroupPreviewResource.class))),
            @ApiResponse(responseCode = "400", description = "Malformed code",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The code does not exist or has expired",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> previewGroup(@PathVariable String code) {
        return savingsGroupQueryService.handle(new GetGroupPreviewQuery(code))
                .<ResponseEntity<?>>map(preview -> ResponseEntity.ok(
                        GroupPreviewResourceFromEntityAssembler.toResourceFromEntity(preview)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Invitation", code)));
    }

    @PostMapping("/invitations/{code}/join")
    @Operation(summary = "Join a group with an invitation code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Joined",
                    content = @Content(schema = @Schema(implementation = GroupResource.class))),
            @ApiResponse(responseCode = "404", description = "The code does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422",
                    description = "Expired invitation, full or started group, or already a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> joinGroup(@AuthenticationPrincipal AuthenticatedMember member,
                                       @PathVariable String code) {
        var result = savingsGroupCommandService.handle(new JoinGroupCommand(code, member.accountId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                group -> GroupResourceFromEntityAssembler.toResourceFromEntity(group, member.accountId()),
                HttpStatus.OK);
    }

    @GetMapping("/groups/{groupId}/members")
    @Operation(summary = "List the members", description = "Only for members of the group; the organizer comes first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Members of the group",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MembershipResource.class)))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getMembers(@AuthenticationPrincipal AuthenticatedMember member,
                                        @PathVariable UUID groupId) {
        return savingsGroupQueryService.handle(new GetMembersQuery(groupId, member.accountId()))
                .<ResponseEntity<?>>map(group -> ResponseEntity.ok(
                        MembershipResourceFromEntityAssembler.toResourcesFromEntity(
                                group, member.accountId(), externalIamService.fetchPhotoUrls(group))))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("SavingsGroup", groupId.toString())));
    }

    @PostMapping("/groups/{groupId}/members/manual")
    @Operation(summary = "Register a member without the application",
            description = "Only the organizer, who then records that member's contributions.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member registered; returns the updated member list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MembershipResource.class)))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The group is full or already started",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> addManualMember(@AuthenticationPrincipal AuthenticatedMember member,
                                             @PathVariable UUID groupId,
                                             @Valid @RequestBody AddManualMemberResource resource) {
        var result = savingsGroupCommandService.handle(
                GroupCommandFromResourceAssembler.toCommandFromResource(groupId, member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                group -> MembershipResourceFromEntityAssembler.toResourcesFromEntity(
                        group, member.accountId(), externalIamService.fetchPhotoUrls(group)),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/groups/{groupId}/members/{membershipId}")
    @Operation(summary = "Remove a member", description = "Only the organizer, and only before the group starts.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Member removed", content = @Content),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The group or the membership does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The group started, or the member is the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> removeMember(@AuthenticationPrincipal AuthenticatedMember member,
                                          @PathVariable UUID groupId,
                                          @PathVariable UUID membershipId) {
        var result = savingsGroupCommandService.handle(
                new RemoveMemberCommand(groupId, member.accountId(), membershipId));
        return ResponseEntityAssembler.toNoContentResponseEntityFromResult(result);
    }
}
