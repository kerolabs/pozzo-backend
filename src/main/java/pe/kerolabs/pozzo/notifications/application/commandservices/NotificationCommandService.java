package pe.kerolabs.pozzo.notifications.application.commandservices;

import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Device;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.ReminderPlan;
import pe.kerolabs.pozzo.notifications.domain.model.commands.CancelRemindersCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.ConfigureReminderPlanCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.DeactivateDeviceCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.DispatchDueNotificationsCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.RegisterDeviceCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.ScheduleRemindersCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.SendAlertCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for devices, reminder plans and the notifications themselves.
 */
public interface NotificationCommandService {

    /**
     * Registers a device push token; moves existing registration to the requester if token is already known.
     *
     * @param command command containing account ID, push token, and device platform
     * @return the registered device aggregate, or an error
     */
    Result<Device, ApplicationError> handle(RegisterDeviceCommand command);

    /**
     * Deactivates a device registration so it no longer receives push notifications.
     *
     * @param command command containing device ID and account ID
     * @return the deactivated device aggregate, or an error
     */
    Result<Device, ApplicationError> handle(DeactivateDeviceCommand command);

    /**
     * Updates the automated reminder plan configuration for a savings group.
     *
     * @param command command containing group ID, organizer account ID, day offsets, dispatch hour, and enabled flag
     * @return the configured reminder plan aggregate, or an error
     */
    Result<ReminderPlan, ApplicationError> handle(ConfigureReminderPlanCommand command);

    /**
     * Schedules the reminders of a period; returns how many were scheduled. Moments already past are skipped.
     *
     * @param command command containing group ID, period ID, and cutoff date
     * @return number of reminder notifications scheduled, or an error
     */
    Result<Integer, ApplicationError> handle(ScheduleRemindersCommand command);

    /**
     * Cancels the pending reminders of a member for a period; returns how many were cancelled.
     *
     * @param command command containing group ID, period ID, and member account ID
     * @return number of reminder notifications cancelled, or an error
     */
    Result<Integer, ApplicationError> handle(CancelRemindersCommand command);

    /**
     * Queues an alert notification for the next dispatch.
     *
     * @param command command containing recipient, title, body, and notification kind
     * @return true if queued, false if duplicate/already sent, or an error
     */
    Result<Boolean, ApplicationError> handle(SendAlertCommand command);

    /**
     * Dispatches all queued notifications that are currently due for delivery.
     *
     * @param command command triggering the batch dispatch run
     * @return number of notifications dispatched, or an error
     */
    Result<Integer, ApplicationError> handle(DispatchDueNotificationsCommand command);
}
