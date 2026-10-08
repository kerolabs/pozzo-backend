package pe.kerolabs.pozzo.notifications.domain.model.queries;

import java.util.UUID;

/**
 * Query for the notifications already sent to the requester, newest first.
 */
public record GetMyNotificationsQuery(UUID accountId, int limit) {
}
