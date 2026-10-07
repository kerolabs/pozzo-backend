package pe.kerolabs.pozzo.notifications.domain.repositories;

import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Device;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Device repository port.
 */
public interface DeviceRepository {

    Optional<Device> findById(UUID id);

    Optional<Device> findByPushToken(String pushToken);

    List<Device> findActiveByAccountId(UUID accountId);

    Device save(Device device);
}
