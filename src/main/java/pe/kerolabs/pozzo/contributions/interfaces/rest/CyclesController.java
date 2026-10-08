package pe.kerolabs.pozzo.contributions.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.contributions.application.queryservices.ContributionQueryService;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetCurrentPeriodQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetCycleByGroupIdQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetMemberContributionsQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetPeriodsQuery;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.CycleResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.MyContributionsResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.PeriodStatusResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.transform.CycleResourceFromEntityAssembler;
import pe.kerolabs.pozzo.contributions.interfaces.rest.transform.MyContributionsResourceFromViewAssembler;
import pe.kerolabs.pozzo.contributions.interfaces.rest.transform.PeriodStatusResourceFromViewAssembler;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Read side of a cycle: the cycle of a group, the pot of the period in progress, the history of
 * periods and the requester's own contributions. Only for the members of the cycle.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Cycles", description = "State of the pot and history of a started savings group")
public class CyclesController {

    static final ZoneId GROUP_ZONE = ZoneId.of("America/Lima");

    private final ContributionQueryService contributionQueryService;
    private final Clock clock;

    public CyclesController(ContributionQueryService contributionQueryService, Clock clock) {
        this.contributionQueryService = contributionQueryService;
        this.clock = clock;
    }

    /**
     * Retrieves the financial cycle associated with an active savings group.
     *
     * @param member the authenticated member making the request (must belong to group)
     * @param groupId the identifier of the savings group
     * @return 200 OK with cycle details, or 404 if not found or unauthorized
     */
    @GetMapping("/groups/{groupId}/cycle")
    @Operation(summary = "Get the cycle of a group", description = "Available once the group has started.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cycle found",
                    content = @Content(schema = @Schema(implementation = CycleResource.class))),
            @ApiResponse(responseCode = "404", description = "The group has not started or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getCycle(@AuthenticationPrincipal AuthenticatedMember member, @PathVariable UUID groupId) {
        return contributionQueryService.handle(new GetCycleByGroupIdQuery(groupId, member.accountId()))
                .<ResponseEntity<?>>map(cycle -> ResponseEntity.ok(
                        CycleResourceFromEntityAssembler.toResourceFromEntity(cycle, member.accountId())))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Cycle", groupId.toString())));
    }

    /**
     * Retrieves the state of the pot and contributions for the currently active period in the cycle.
     *
     * @param member the authenticated member making the request
     * @param cycleId the identifier of the financial cycle
     * @return 200 OK with status of current period, beneficiary, and member contributions
     */
    @GetMapping("/cycles/{cycleId}/periods/current")
    @Operation(summary = "Get the state of the pot",
            description = "The period in progress: who collects, how much is gathered and the state of every member.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "State of the pot",
                    content = @Content(schema = @Schema(implementation = PeriodStatusResource.class))),
            @ApiResponse(responseCode = "404", description = "The cycle does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getCurrentPeriod(@AuthenticationPrincipal AuthenticatedMember member,
                                              @PathVariable UUID cycleId) {
        return contributionQueryService.handle(new GetCurrentPeriodQuery(cycleId, member.accountId()))
                .<ResponseEntity<?>>map(view -> ResponseEntity.ok(
                        PeriodStatusResourceFromViewAssembler.toResourceFromView(view, member.accountId(), today())))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Cycle", cycleId.toString())));
    }

    /**
     * Lists all periods (rounds) in chronological/turn order opened so far for the cycle.
     *
     * @param member the authenticated member making the request
     * @param cycleId the identifier of the financial cycle
     * @return 200 OK with list of period statuses
     */
    @GetMapping("/cycles/{cycleId}/periods")
    @Operation(summary = "List the periods", description = "Every period opened so far, in turn order.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Periods of the cycle",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PeriodStatusResource.class)))),
            @ApiResponse(responseCode = "404", description = "The cycle does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getPeriods(@AuthenticationPrincipal AuthenticatedMember member,
                                        @PathVariable UUID cycleId) {
        var today = today();
        return contributionQueryService.handle(new GetPeriodsQuery(cycleId, member.accountId()))
                .<ResponseEntity<?>>map(views -> ResponseEntity.ok(views.stream()
                        .map(view -> PeriodStatusResourceFromViewAssembler.toResourceFromView(
                                view, member.accountId(), today))
                        .toList()))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Cycle", cycleId.toString())));
    }

    /**
     * Lists the authenticated member's contributions across all periods within the cycle.
     *
     * @param member the authenticated member making the request
     * @param cycleId the identifier of the financial cycle
     * @return 200 OK with member's contribution breakdown
     */
    @GetMapping("/cycles/{cycleId}/members/me/contributions")
    @Operation(summary = "List my contributions", description = "The requester's contributions, period by period.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "My contributions",
                    content = @Content(schema = @Schema(implementation = MyContributionsResource.class))),
            @ApiResponse(responseCode = "404", description = "The cycle does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getMyContributions(@AuthenticationPrincipal AuthenticatedMember member,
                                                @PathVariable UUID cycleId) {
        return contributionQueryService.handle(new GetMemberContributionsQuery(cycleId, member.accountId()))
                .filter(views -> !views.isEmpty())
                .<ResponseEntity<?>>map(views -> ResponseEntity.ok(
                        MyContributionsResourceFromViewAssembler.toResourceFromViews(
                                views.getFirst().cycle(), views, member.accountId(), today())))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Cycle", cycleId.toString())));
    }

    private LocalDate today() {
        return LocalDate.now(clock.withZone(GROUP_ZONE));
    }
}
