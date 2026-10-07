package pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Notification;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationContent;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationStatus;
import pe.kerolabs.pozzo.notifications.domain.repositories.NotificationRepository;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.repositories.NotificationPersistenceRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Adapter that implements the notification repository port with Spring Data JPA.
 */
@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationPersistenceRepository persistenceRepository;

    public NotificationRepositoryImpl(NotificationPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public List<Notification> findDue(Instant now, int limit) {
        return persistenceRepository.findAllByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                        NotificationStatus.SCHEDULED, now, Limit.of(limit)).stream()
                .map(NotificationRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public List<Notification> findScheduledByPeriodIdAndAccountId(UUID periodId, UUID accountId) {
        return persistenceRepository.findAllByPeriodIdAndAccountIdAndStatus(periodId, accountId,
                        NotificationStatus.SCHEDULED).stream()
                .map(NotificationRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public boolean existsByDedupKey(String dedupKey) {
        return persistenceRepository.existsByDedupKey(dedupKey);
    }

    @Override
    public List<Notification> findSentByAccountId(UUID accountId, int limit) {
        return persistenceRepository.findAllByAccountIdAndStatusOrderBySentAtDesc(accountId, NotificationStatus.SENT,
                        Limit.of(limit)).stream()
                .map(NotificationRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public Notification save(Notification notification) {
        var entity = persistenceRepository.findById(notification.getId())
                .orElseGet(NotificationPersistenceEntity::new);
        entity.setId(notification.getId());
        entity.setAccountId(notification.getAccountId());
        entity.setGroupId(notification.getGroupId());
        entity.setPeriodId(notification.getPeriodId());
        entity.setKind(notification.getKind());
        entity.setTitle(notification.getContent().title());
        entity.setBody(notification.getContent().body());
        entity.setDeepLink(notification.getContent().deepLink());
        entity.setDedupKey(notification.getDedupKey());
        entity.setScheduledAt(notification.getScheduledAt());
        entity.setSentAt(notification.getSentAt());
        entity.setStatus(notification.getStatus());
        entity.setAttempts(notification.getAttempts());
        return toDomain(persistenceRepository.save(entity));
    }

    private static Notification toDomain(NotificationPersistenceEntity entity) {
        var notification = new Notification();
        notification.restoreState(entity.getId(), entity.getAccountId(), entity.getGroupId(), entity.getPeriodId(),
                entity.getKind(), new NotificationContent(entity.getTitle(), entity.getBody(), entity.getDeepLink()),
                entity.getDedupKey(), entity.getScheduledAt(), entity.getSentAt(), entity.getStatus(),
                entity.getAttempts());
        return notification;
    }
}
