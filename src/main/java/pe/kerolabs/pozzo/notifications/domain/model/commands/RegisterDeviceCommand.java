package pe.kerolabs.pozzo.notifications.domain.model.commands;

import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.Platform;

import java.util.UUID;

/**
 * Command to register the phone of the requester for push notifications.
 */
public record RegisterDeviceCommand(UUID accountId, String pushToken, Platform platform) {
}
