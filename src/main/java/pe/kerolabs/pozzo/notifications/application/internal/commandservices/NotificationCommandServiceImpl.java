package pe.kerolabs.pozzo.notifications.application.internal.commandservices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.notifications.application.commandservices.NotificationCommandService;
import pe.kerolabs.pozzo.notifications.application.internal.outboundservices.acl.ExternalSavingsGroupsService;
import pe.kerolabs.pozzo.notifications.application.internal.outboundservices.push.PushSender;
import pe.kerolabs.pozzo.notifications.application.internal.outboundservices.push.PushSender.PushOutcome;
import pe.kerolabs.pozzo.notifications.application.internal.texts.NotificationTexts;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Device;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Notification;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.ReminderPlan;
import pe.kerolabs.pozzo.notifications.domain.model.commands.CancelRemindersCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.ConfigureReminderPlanCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.DeactivateDeviceCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.DispatchDueNotificationsCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.RegisterDeviceCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.ScheduleRemindersCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.SendAlertCommand;
import pe.kerolabs.pozzo.notifications.domain.repositories.DeviceRepository;
import pe.kerolabs.pozzo.notifications.domain.repositories.NotificationRepository;
import pe.kerolabs.pozzo.notifications.domain.repositories.ReminderPlanRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Registers devices, keeps the reminder plans, queues reminders and alerts and sends them.
 */
@Slf4j
@Service
@Transactional
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final DeviceRepository deviceRepository;
    private final ReminderPlanRepository reminderPlanRepository;
    private final NotificationRepository notificationRepository;
    private final ExternalSavingsGroupsService externalSavingsGroupsService;
    private final PushSender pushSender;
    private final Clock clock;
    private final ZoneId zone;

    public NotificationCommandServiceImpl(DeviceRepository deviceRepository,
                                          ReminderPlanRepository reminderPlanRepository,
                                          NotificationRepository notificationRepository,
                                          ExternalSavingsGroupsService externalSavingsGroupsService,
                                          PushSender pushSender,
                                          Clock clock,
                                          @Value("${notifications.time-zone:America/Lima}") String zone) {
        this.deviceRepository = deviceRepository;
        this.reminderPlanRepository = reminderPlanRepository;
        this.notificationRepository = notificationRepository;
        this.externalSavingsGroupsService = externalSavingsGroupsService;
        this.pushSender = pushSender;
        this.clock = clock;
        this.zone = ZoneId.of(zone);
    }

    @Override
    public Result<Device, ApplicationError> handle(RegisterDeviceCommand command) {
        var now = clock.instant();
        Device device;
        try {
            device = deviceRepository.findByPushToken(command.pushToken().strip())
                    .map(existing -> {
                        existing.assignTo(command.accountId(), command.platform(), now);
                        return existing;
                    })
                    .orElseGet(() -> Device.register(command.accountId(), command.pushToken(), command.platform(), now));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("pushToken", e.getMessage()));
        }
        return Result.success(deviceRepository.save(device));
    }

    @Override
    public Result<Device, ApplicationError> handle(DeactivateDeviceCommand command) {
        return deviceRepository.findById(command.deviceId())
                .filter(device -> device.getAccountId().equals(command.accountId()))
                .<Result<Device, ApplicationError>>map(device -> {
                    device.deactivate();
                    return Result.success(deviceRepository.save(device));
                })
                .orElseGet(() -> Result.failure(ApplicationError.notFound("Device", command.deviceId().toString())));
    }

    @Override
    public Result<ReminderPlan, ApplicationError> handle(ConfigureReminderPlanCommand command) {
        if (!externalSavingsGroupsService.isMember(command.groupId(), command.requesterAccountId())) {
            return Result.failure(ApplicationError.notFound("SavingsGroup", command.groupId().toString()));
        }
        if (!externalSavingsGroupsService.isOrganizer(command.groupId(), command.requesterAccountId())) {
            return Result.failure(ApplicationError.forbidden(
                    "ONLY_ORGANIZER", "Only the organizer of the group can do this"));
        }
        var now = clock.instant();
        var plan = reminderPlanRepository.findByGroupId(command.groupId())
                .orElseGet(() -> ReminderPlan.defaultFor(command.groupId(), now));
        try {
            plan.configure(command.offsetsInDays(), command.sendHour(), command.enabled(), now);
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("reminderPlan", e.getMessage()));
        }
        return Result.success(reminderPlanRepository.save(plan));
    }

    @Override
    public Result<Integer, ApplicationError> handle(ScheduleRemindersCommand command) {
        var now = clock.instant();
        var plan = reminderPlanRepository.findByGroupId(command.groupId())
                .orElseGet(() -> ReminderPlan.defaultFor(command.groupId(), now));
        var scheduled = 0;
        for (var entry : plan.scheduleFor(command.cutoffDate(), zone).entrySet()) {
            if (entry.getValue().isBefore(now)) {
                continue;
            }
            var content = NotificationTexts.reminder(entry.getKey(), command.groupName(),
                    command.contributionAmount(), command.cutoffDate(), command.groupId());
            for (var accountId : command.accountIds()) {
                var dedupKey = "reminder:%s:%s:%d".formatted(command.periodId(), accountId, entry.getKey());
                if (notificationRepository.existsByDedupKey(dedupKey)) {
                    continue;
                }
                notificationRepository.save(Notification.reminder(accountId, command.groupId(), command.periodId(),
                        content, dedupKey, entry.getValue()));
                scheduled++;
            }
        }
        return Result.success(scheduled);
    }

    @Override
    public Result<Integer, ApplicationError> handle(CancelRemindersCommand command) {
        var reminders = notificationRepository.findScheduledRemindersByPeriodIdAndAccountId(command.periodId(),
                command.accountId());
        reminders.forEach(reminder -> {
            reminder.cancel();
            notificationRepository.save(reminder);
        });
        return Result.success(reminders.size());
    }

    @Override
    public Result<Boolean, ApplicationError> handle(SendAlertCommand command) {
        if (notificationRepository.existsByDedupKey(command.dedupKey())) {
            return Result.success(false);
        }
        var now = clock.instant();
        var alert = notificationRepository.save(Notification.alert(command.accountId(), command.groupId(),
                command.periodId(), command.content(), command.dedupKey(), now));
        // An alert tells something that just happened, so it goes out now instead of at the next dispatch.
        deliver(alert, now);
        return Result.success(true);
    }

    @Override
    public Result<Integer, ApplicationError> handle(DispatchDueNotificationsCommand command) {
        var now = clock.instant();
        var sent = 0;
        for (var notification : notificationRepository.findDue(now, command.limit())) {
            if (deliver(notification, now)) {
                sent++;
            }
        }
        return Result.success(sent);
    }

    /**
     * Pushes a notification to every active device of its member and records the outcome.
     *
     * @return true when it counts as sent
     */
    private boolean deliver(Notification notification, Instant now) {
        var delivered = 0;
        var failed = 0;
        for (var device : deviceRepository.findActiveByAccountId(notification.getAccountId())) {
            switch (send(device, notification)) {
                case DELIVERED -> delivered++;
                case FAILED -> failed++;
                case INVALID_TOKEN -> {
                    device.deactivate();
                    deviceRepository.save(device);
                }
            }
        }
        // A member without devices still finds the notification in the application.
        var sent = delivered > 0 || failed == 0;
        if (sent) {
            notification.markSent(now);
        } else {
            notification.markFailed();
        }
        notificationRepository.save(notification);
        return sent;
    }

    private PushOutcome send(Device device, Notification notification) {
        try {
            var content = notification.getContent();
            return pushSender.send(device.getPushToken(), notification.getId(), content.title(), content.body(),
                    content.deepLink());
        } catch (RuntimeException e) {
            log.warn("Push delivery of notification {} failed", notification.getId(), e);
            return PushOutcome.FAILED;
        }
    }
}
