package pe.kerolabs.pozzo.notifications.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.notifications.application.queryservices.NotificationQueryService;
import pe.kerolabs.pozzo.notifications.domain.model.queries.GetMyNotificationsQuery;
import pe.kerolabs.pozzo.notifications.interfaces.rest.resources.NotificationResource;
import pe.kerolabs.pozzo.notifications.interfaces.rest.transform.NotificationResourceAssembler;

import java.util.List;

/**
 * The notifications already sent to the authenticated member.
 */
@RestController
@RequestMapping(value = "/api/v1/members/me/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Devices, reminders and alerts")
public class NotificationsController {

    private static final int LIMIT = 50;

    private final NotificationQueryService notificationQueryService;

    public NotificationsController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping
    @Operation(summary = "Get my notifications", description = "The last 50 sent to me, newest first.")
    @ApiResponse(responseCode = "200", description = "My notifications",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationResource.class))))
    public ResponseEntity<List<NotificationResource>> getMyNotifications(
            @AuthenticationPrincipal AuthenticatedMember member) {
        return ResponseEntity.ok(notificationQueryService.handle(new GetMyNotificationsQuery(member.accountId(), LIMIT))
                .stream()
                .map(NotificationResourceAssembler::toResourceFromNotification)
                .toList());
    }
}
