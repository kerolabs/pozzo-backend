package pe.kerolabs.pozzo.notifications.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.notifications.application.internal.outboundservices.acl.ExternalSavingsGroupsService;
import pe.kerolabs.pozzo.notifications.application.queryservices.NotificationQueryService;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Notification;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.ReminderPlan;
import pe.kerolabs.pozzo.notifications.domain.model.queries.GetMyNotificationsQuery;
import pe.kerolabs.pozzo.notifications.domain.model.queries.GetReminderPlanQuery;
import pe.kerolabs.pozzo.notifications.domain.repositories.NotificationRepository;
import pe.kerolabs.pozzo.notifications.domain.repositories.ReminderPlanRepository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * Resolves the notification queries.
 */
@Service
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final ReminderPlanRepository reminderPlanRepository;
    private final ExternalSavingsGroupsService externalSavingsGroupsService;
    private final Clock clock;

    public NotificationQueryServiceImpl(NotificationRepository notificationRepository,
                                        ReminderPlanRepository reminderPlanRepository,
                                        ExternalSavingsGroupsService externalSavingsGroupsService,
                                        Clock clock) {
        this.notificationRepository = notificationRepository;
        this.reminderPlanRepository = reminderPlanRepository;
        this.externalSavingsGroupsService = externalSavingsGroupsService;
        this.clock = clock;
    }

    @Override
    public List<Notification> handle(GetMyNotificationsQuery query) {
        return notificationRepository.findSentByAccountId(query.accountId(), query.limit());
    }

    @Override
    public Optional<ReminderPlan> handle(GetReminderPlanQuery query) {
        if (!externalSavingsGroupsService.isMember(query.groupId(), query.requesterAccountId())) {
            return Optional.empty();
        }
        return Optional.of(reminderPlanRepository.findByGroupId(query.groupId())
                .orElseGet(() -> ReminderPlan.defaultFor(query.groupId(), clock.instant())));
    }
}
