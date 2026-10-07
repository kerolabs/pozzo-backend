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
     * Registers a phone; a token already known moves to the requester.
     */
    Result<Device, ApplicationError> handle(RegisterDeviceCommand command);

    Result<Device, ApplicationError> handle(DeactivateDeviceCommand command);

    Result<ReminderPlan, ApplicationError> handle(ConfigureReminderPlanCommand command);

    /**
     * Schedules the reminders of a period; returns how many were scheduled. Moments already past are skipped.
     */
    Result<Integer, ApplicationError> handle(ScheduleRemindersCommand command);

    /**
     * Cancels the pending reminders of a member for a period; returns how many were cancelled.
     */
    Result<Integer, ApplicationError> handle(CancelRemindersCommand command);

    /**
     * Queues an alert for the next dispatch; returns false when the alert was already sent before.
     */
    Result<Boolean, ApplicationError> handle(SendAlertCommand command);

    /**
     * Sends the notifications whose moment arrived; returns how many went out.
     */
    Result<Integer, ApplicationError> handle(DispatchDueNotificationsCommand command);
}
