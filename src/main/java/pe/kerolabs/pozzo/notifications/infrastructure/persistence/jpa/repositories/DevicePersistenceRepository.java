package pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.entities.DevicePersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for device persistence entities.
 */
@Repository
public interface DevicePersistenceRepository extends JpaRepository<DevicePersistenceEntity, UUID> {

    Optional<DevicePersistenceEntity> findByPushToken(String pushToken);

    List<DevicePersistenceEntity> findAllByAccountIdAndActiveTrue(UUID accountId);
}
