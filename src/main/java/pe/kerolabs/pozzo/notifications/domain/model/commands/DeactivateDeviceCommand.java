package pe.kerolabs.pozzo.notifications.domain.model.commands;

import java.util.UUID;

/**
 * Command to stop sending push notifications to one of the requester's phones.
 */
public record DeactivateDeviceCommand(UUID deviceId, UUID accountId) {
}
