package pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Device;
import pe.kerolabs.pozzo.notifications.domain.repositories.DeviceRepository;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.entities.DevicePersistenceEntity;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.repositories.DevicePersistenceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the device repository port with Spring Data JPA.
 */
@Repository
public class DeviceRepositoryImpl implements DeviceRepository {

    private final DevicePersistenceRepository persistenceRepository;

    public DeviceRepositoryImpl(DevicePersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<Device> findById(UUID id) {
        return persistenceRepository.findById(id).map(DeviceRepositoryImpl::toDomain);
    }

    @Override
    public Optional<Device> findByPushToken(String pushToken) {
        return persistenceRepository.findByPushToken(pushToken).map(DeviceRepositoryImpl::toDomain);
    }

    @Override
    public List<Device> findActiveByAccountId(UUID accountId) {
        return persistenceRepository.findAllByAccountIdAndActiveTrue(accountId).stream()
                .map(DeviceRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public Device save(Device device) {
        var entity = persistenceRepository.findById(device.getId()).orElseGet(DevicePersistenceEntity::new);
        entity.setId(device.getId());
        entity.setAccountId(device.getAccountId());
        entity.setPushToken(device.getPushToken());
        entity.setPlatform(device.getPlatform());
        entity.setRegisteredAt(device.getRegisteredAt());
        entity.setActive(device.isActive());
        return toDomain(persistenceRepository.save(entity));
    }

    private static Device toDomain(DevicePersistenceEntity entity) {
        var device = new Device();
        device.restoreState(entity.getId(), entity.getAccountId(), entity.getPushToken(), entity.getPlatform(),
                entity.getRegisteredAt(), entity.isActive());
        return device;
    }
}
