package pe.kerolabs.pozzo.savingsgroups.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.savingsgroups.application.commandservices.TurnCommandService;
import pe.kerolabs.pozzo.savingsgroups.application.internal.outboundservices.acl.ExternalIamService;
import pe.kerolabs.pozzo.savingsgroups.application.queryservices.SavingsGroupQueryService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AssignTurnsAgreedCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AssignTurnsByDrawCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetTurnCalendarQuery;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.AgreedTurnsResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.TurnCalendarResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform.TurnCalendarResourceFromEntityAssembler;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.UUID;

/**
 * Collection order of a savings group: draw it, set the agreed order and read the calendar.
 */
@RestController
@RequestMapping(value = "/api/v1/groups/{groupId}/turns", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Turns", description = "Collection order of a savings group")
public class TurnsController {

    private final TurnCommandService turnCommandService;
    private final SavingsGroupQueryService savingsGroupQueryService;
    private final ExternalIamService externalIamService;

    public TurnsController(TurnCommandService turnCommandService, SavingsGroupQueryService savingsGroupQueryService,
                           ExternalIamService externalIamService) {
        this.turnCommandService = turnCommandService;
        this.savingsGroupQueryService = savingsGroupQueryService;
        this.externalIamService = externalIamService;
    }

    /**
     * Executes a pseudo-random draw (lottery) to assign the turn sequence for all enrolled members.
     * The random seed is published transparently so participants can audit the draw result.
     * Only permitted for the organizer when all seats are filled.
     *
     * @param member the authenticated organizer
     * @param groupId the identifier of the savings group
     * @return 200 OK with the generated turn calendar resource, or error status
     */
    @PostMapping("/draw")
    @Operation(summary = "Draw the turns",
            description = "Only the organizer, with every seat taken. Running it again repeats the draw. "
                    + "The seed is published so any member can reproduce the result.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turns drawn",
                    content = @Content(schema = @Schema(implementation = TurnCalendarResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The group is not full or already started",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> drawTurns(@AuthenticationPrincipal AuthenticatedMember member,
                                       @PathVariable UUID groupId) {
        var result = turnCommandService.handle(new AssignTurnsByDrawCommand(groupId, member.accountId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                group -> TurnCalendarResourceFromEntityAssembler.toResourceFromEntity(
                        group, member.accountId(), externalIamService.fetchPhotoUrls(group)),
                HttpStatus.OK);
    }

    /**
     * Sets a manually agreed turn sequence for the savings group.
     * Every enrolled member must be allocated exactly one turn number.
     * Only permitted for the organizer when all seats are filled.
     *
     * @param member the authenticated organizer
     * @param groupId the identifier of the savings group
     * @param resource payload containing the explicit order of membership identifiers
     * @return 200 OK with the assigned turn calendar resource, or error status
     */
    @PostMapping("/agreed")
    @Operation(summary = "Set the agreed order",
            description = "Only the organizer, with every seat taken. Every member must appear exactly once.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order set",
                    content = @Content(schema = @Schema(implementation = TurnCalendarResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Invalid order, group not full or already started",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> agreedTurns(@AuthenticationPrincipal AuthenticatedMember member,
                                         @PathVariable UUID groupId,
                                         @Valid @RequestBody AgreedTurnsResource resource) {
        var result = turnCommandService.handle(
                new AssignTurnsAgreedCommand(groupId, member.accountId(), resource.order()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                group -> TurnCalendarResourceFromEntityAssembler.toResourceFromEntity(
                        group, member.accountId(), externalIamService.fetchPhotoUrls(group)),
                HttpStatus.OK);
    }

    /**
     * Retrieves the complete turn calendar showing dates, turns, and assigned beneficiaries.
     * Accessible by enrolled members of the group.
     *
     * @param member the authenticated member making the request
     * @param groupId the identifier of the savings group
     * @return 200 OK with turn calendar resource, or 404 if not found
     */
    @GetMapping
    @Operation(summary = "Get the turn calendar", description = "Only for members of the group.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turn calendar",
                    content = @Content(schema = @Schema(implementation = TurnCalendarResource.class))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getTurns(@AuthenticationPrincipal AuthenticatedMember member,
                                      @PathVariable UUID groupId) {
        return savingsGroupQueryService.handle(new GetTurnCalendarQuery(groupId, member.accountId()))
                .<ResponseEntity<?>>map(group -> ResponseEntity.ok(
                        TurnCalendarResourceFromEntityAssembler.toResourceFromEntity(
                        group, member.accountId(), externalIamService.fetchPhotoUrls(group))))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("SavingsGroup", groupId.toString())));
    }
}
