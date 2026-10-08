package pe.kerolabs.pozzo.notifications.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.notifications.application.commandservices.NotificationCommandService;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Device;
import pe.kerolabs.pozzo.notifications.domain.model.commands.DeactivateDeviceCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.RegisterDeviceCommand;
import pe.kerolabs.pozzo.notifications.interfaces.rest.resources.DeviceResource;
import pe.kerolabs.pozzo.notifications.interfaces.rest.resources.RegisterDeviceResource;
import pe.kerolabs.pozzo.notifications.interfaces.rest.transform.NotificationResourceAssembler;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.UUID;

/**
 * The phones of the authenticated member that receive push notifications.
 */
@RestController
@RequestMapping(value = "/api/v1/members/me/devices", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Devices, reminders and alerts")
public class DevicesController {

    private final NotificationCommandService notificationCommandService;

    public DevicesController(NotificationCommandService notificationCommandService) {
        this.notificationCommandService = notificationCommandService;
    }

    @PostMapping
    @Operation(summary = "Register my phone",
            description = "Registers the Firebase token of the phone. A token already registered moves to the requester.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Phone registered",
                    content = @Content(schema = @Schema(implementation = DeviceResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid token or platform",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> registerDevice(@AuthenticationPrincipal AuthenticatedMember member,
                                            @Valid @RequestBody RegisterDeviceResource resource) {
        var command = new RegisterDeviceCommand(member.accountId(), resource.pushToken(), resource.platform());
        var result = registerOnce(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                NotificationResourceAssembler::toResourceFromDevice, HttpStatus.CREATED);
    }

    /**
     * The app may register the same token twice at once (the session opens and Firebase hands the token at the
     * same moment): both find no device and both insert, and the second breaks the unique token. That one is
     * retried in a new transaction, where it finds the device the first one saved and updates it.
     */
    private Result<Device, ApplicationError> registerOnce(RegisterDeviceCommand command) {
        try {
            return notificationCommandService.handle(command);
        } catch (DataIntegrityViolationException concurrentRegistration) {
            return notificationCommandService.handle(command);
        }
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "Remove my phone", description = "The phone stops receiving push notifications.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Phone removed", content = @Content),
            @ApiResponse(responseCode = "404", description = "The phone does not exist or is not the requester's",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> deactivateDevice(@AuthenticationPrincipal AuthenticatedMember member,
                                              @PathVariable UUID deviceId) {
        return ResponseEntityAssembler.toNoContentResponseEntityFromResult(
                notificationCommandService.handle(new DeactivateDeviceCommand(deviceId, member.accountId())));
    }
}
