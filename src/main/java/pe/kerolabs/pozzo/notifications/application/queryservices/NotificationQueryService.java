package pe.kerolabs.pozzo.notifications.application.queryservices;

import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Notification;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.ReminderPlan;
import pe.kerolabs.pozzo.notifications.domain.model.queries.GetMyNotificationsQuery;
import pe.kerolabs.pozzo.notifications.domain.model.queries.GetReminderPlanQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for reading notifications and reminder plans.
 */
public interface NotificationQueryService {

    List<Notification> handle(GetMyNotificationsQuery query);

    /**
     * The plan of the group, or the default one if the organizer never changed it. Empty when the
     * requester is not a member.
     */
    Optional<ReminderPlan> handle(GetReminderPlanQuery query);
}
