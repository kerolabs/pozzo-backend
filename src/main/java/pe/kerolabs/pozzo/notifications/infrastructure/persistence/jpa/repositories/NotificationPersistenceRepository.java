package pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.repositories;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationKind;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationStatus;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for notification persistence entities.
 */
@Repository
public interface NotificationPersistenceRepository extends JpaRepository<NotificationPersistenceEntity, UUID> {

    List<NotificationPersistenceEntity> findAllByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
            NotificationStatus status, Instant now, Limit limit);

    List<NotificationPersistenceEntity> findAllByPeriodIdAndAccountIdAndKindAndStatus(UUID periodId, UUID accountId,
                                                                                      NotificationKind kind,
                                                                                      NotificationStatus status);

    boolean existsByDedupKey(String dedupKey);

    List<NotificationPersistenceEntity> findAllByAccountIdAndStatusOrderBySentAtDesc(UUID accountId,
                                                                                    NotificationStatus status,
                                                                                    Limit limit);
}
