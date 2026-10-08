package pe.kerolabs.pozzo.notifications.interfaces.rest.transform;

import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Device;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Notification;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.ReminderPlan;
import pe.kerolabs.pozzo.notifications.interfaces.rest.resources.DeviceResource;
import pe.kerolabs.pozzo.notifications.interfaces.rest.resources.NotificationResource;
import pe.kerolabs.pozzo.notifications.interfaces.rest.resources.ReminderPlanResource;

/**
 * Turns devices, reminder plans and notifications into REST resources.
 */
public final class NotificationResourceAssembler {

    private NotificationResourceAssembler() {
    }

    public static DeviceResource toResourceFromDevice(Device device) {
        return new DeviceResource(device.getId(), device.getPlatform(), device.getRegisteredAt(), device.isActive());
    }

    public static ReminderPlanResource toResourceFromReminderPlan(ReminderPlan plan) {
        return new ReminderPlanResource(plan.getGroupId(), plan.getOffsetsInDays(), plan.getSendHour(),
                plan.isEnabled(), plan.getUpdatedAt());
    }

    public static NotificationResource toResourceFromNotification(Notification notification) {
        var content = notification.getContent();
        return new NotificationResource(notification.getId(), notification.getGroupId(), notification.getPeriodId(),
                notification.getKind(), content.title(), content.body(), content.deepLink(),
                notification.getSentAt());
    }
}
