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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.savingsgroups.application.commandservices.SavingsGroupCommandService;
import pe.kerolabs.pozzo.savingsgroups.application.queryservices.SavingsGroupQueryService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.StartGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetGroupByIdQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetMyGroupsQuery;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.CreateGroupResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.DestinationResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.GroupResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.UpdateRulesResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform.GroupCommandFromResourceAssembler;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform.GroupResourceFromEntityAssembler;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.List;
import java.util.UUID;

/**
 * Lifecycle of a savings group: create it, read it, list my groups, adjust its rules and start it.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Savings Groups", description = "Lifecycle of a savings group")
public class SavingsGroupsController {

    private final SavingsGroupCommandService savingsGroupCommandService;
    private final SavingsGroupQueryService savingsGroupQueryService;

    public SavingsGroupsController(SavingsGroupCommandService savingsGroupCommandService,
                                   SavingsGroupQueryService savingsGroupQueryService) {
        this.savingsGroupCommandService = savingsGroupCommandService;
        this.savingsGroupQueryService = savingsGroupQueryService;
    }

    @PostMapping("/groups")
    @Operation(summary = "Create a savings group", description = "The authenticated member becomes its organizer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Group created",
                    content = @Content(schema = @Schema(implementation = GroupResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid rules",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The first contribution date is in the past",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> createGroup(@AuthenticationPrincipal AuthenticatedMember member,
                                         @Valid @RequestBody CreateGroupResource resource) {
        var result = savingsGroupCommandService.handle(
                GroupCommandFromResourceAssembler.toCommandFromResource(member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                group -> GroupResourceFromEntityAssembler.toResourceFromEntity(group, member.accountId()),
                HttpStatus.CREATED);
    }

    @GetMapping("/groups/{groupId}")
    @Operation(summary = "Get a savings group", description = "Only its members can see it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group found",
                    content = @Content(schema = @Schema(implementation = GroupResource.class))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getGroup(@AuthenticationPrincipal AuthenticatedMember member,
                                      @PathVariable UUID groupId) {
        return savingsGroupQueryService.handle(new GetGroupByIdQuery(groupId, member.accountId()))
                .<ResponseEntity<?>>map(group -> ResponseEntity.ok(
                        GroupResourceFromEntityAssembler.toResourceFromEntity(group, member.accountId())))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("SavingsGroup", groupId.toString())));
    }

    @GetMapping("/members/me/groups")
    @Operation(summary = "List my savings groups", description = "Groups where the member has an active membership.")
    @ApiResponse(responseCode = "200", description = "Groups of the member",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = GroupResource.class))))
    public ResponseEntity<List<GroupResource>> getMyGroups(@AuthenticationPrincipal AuthenticatedMember member) {
        var groups = savingsGroupQueryService.handle(new GetMyGroupsQuery(member.accountId())).stream()
                .map(group -> GroupResourceFromEntityAssembler.toResourceFromEntity(group, member.accountId()))
                .toList();
        return ResponseEntity.ok(groups);
    }

    @PutMapping("/groups/{groupId}/rules")
    @Operation(summary = "Adjust the rules", description = "Only the organizer, and only before the group starts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rules updated",
                    content = @Content(schema = @Schema(implementation = GroupResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The group started, or fewer seats than members",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> updateRules(@AuthenticationPrincipal AuthenticatedMember member,
                                         @PathVariable UUID groupId,
                                         @Valid @RequestBody UpdateRulesResource resource) {
        var result = savingsGroupCommandService.handle(
                GroupCommandFromResourceAssembler.toCommandFromResource(groupId, member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                group -> GroupResourceFromEntityAssembler.toResourceFromEntity(group, member.accountId()),
                HttpStatus.OK);
    }

    @PatchMapping("/groups/{groupId}/destination")
    @Operation(summary = "Define where the contributions are sent",
            description = "Only the organizer, and only before the group starts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Destination defined",
                    content = @Content(schema = @Schema(implementation = GroupResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> defineDestination(@AuthenticationPrincipal AuthenticatedMember member,
                                               @PathVariable UUID groupId,
                                               @Valid @RequestBody DestinationResource resource) {
        var result = savingsGroupCommandService.handle(
                GroupCommandFromResourceAssembler.toCommandFromResource(groupId, member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                group -> GroupResourceFromEntityAssembler.toResourceFromEntity(group, member.accountId()),
                HttpStatus.OK);
    }

    @PostMapping("/groups/{groupId}/start")
    @Operation(summary = "Start the group",
            description = "Requires every seat taken, the turns assigned and the destination defined. "
                    + "The rules can no longer change and the invitation expires.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group started",
                    content = @Content(schema = @Schema(implementation = GroupResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The group is not ready or already started",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> startGroup(@AuthenticationPrincipal AuthenticatedMember member,
                                        @PathVariable UUID groupId) {
        var result = savingsGroupCommandService.handle(new StartGroupCommand(groupId, member.accountId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                group -> GroupResourceFromEntityAssembler.toResourceFromEntity(group, member.accountId()),
                HttpStatus.OK);
    }
}
