package pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.entities.ReminderPlanPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for reminder plan persistence entities.
 */
@Repository
public interface ReminderPlanPersistenceRepository extends JpaRepository<ReminderPlanPersistenceEntity, UUID> {

    Optional<ReminderPlanPersistenceEntity> findByGroupId(UUID groupId);
}
