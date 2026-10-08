package pe.kerolabs.pozzo.notifications.interfaces.rest;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.notifications.application.commandservices.NotificationCommandService;
import pe.kerolabs.pozzo.notifications.application.queryservices.NotificationQueryService;
import pe.kerolabs.pozzo.notifications.domain.model.commands.ConfigureReminderPlanCommand;
import pe.kerolabs.pozzo.notifications.domain.model.queries.GetReminderPlanQuery;
import pe.kerolabs.pozzo.notifications.interfaces.rest.resources.ConfigureReminderPlanResource;
import pe.kerolabs.pozzo.notifications.interfaces.rest.resources.ReminderPlanResource;
import pe.kerolabs.pozzo.notifications.interfaces.rest.transform.NotificationResourceAssembler;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.UUID;

/**
 * The reminders a group sends before each cutoff date.
 */
@RestController
@RequestMapping(value = "/api/v1/groups/{groupId}/reminder-plan", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Devices, reminders and alerts")
public class ReminderPlansController {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;

    public ReminderPlansController(NotificationCommandService notificationCommandService,
                                   NotificationQueryService notificationQueryService) {
        this.notificationCommandService = notificationCommandService;
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping
    @Operation(summary = "Get the reminders of a group",
            description = "Members only. Without changes, three days, one day and the same day, at 9:00.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reminder plan",
                    content = @Content(schema = @Schema(implementation = ReminderPlanResource.class))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getReminderPlan(@AuthenticationPrincipal AuthenticatedMember member,
                                             @PathVariable UUID groupId) {
        return notificationQueryService.handle(new GetReminderPlanQuery(groupId, member.accountId()))
                .<ResponseEntity<?>>map(plan -> ResponseEntity.ok(
                        NotificationResourceAssembler.toResourceFromReminderPlan(plan)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("SavingsGroup", groupId.toString())));
    }

    @PutMapping
    @Operation(summary = "Change the reminders of a group",
            description = "Only the organizer. Applies to the periods that open afterwards.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reminder plan changed",
                    content = @Content(schema = @Schema(implementation = ReminderPlanResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid days or hour",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The group does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> configureReminderPlan(@AuthenticationPrincipal AuthenticatedMember member,
                                                   @PathVariable UUID groupId,
                                                   @Valid @RequestBody ConfigureReminderPlanResource resource) {
        var result = notificationCommandService.handle(new ConfigureReminderPlanCommand(groupId, member.accountId(),
                resource.offsetsInDays(), resource.sendHour(), resource.enabled()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                NotificationResourceAssembler::toResourceFromReminderPlan, HttpStatus.OK);
    }
}
