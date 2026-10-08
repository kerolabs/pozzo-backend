package pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.notifications.domain.model.aggregates.ReminderPlan;
import pe.kerolabs.pozzo.notifications.domain.repositories.ReminderPlanRepository;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.entities.ReminderPlanPersistenceEntity;
import pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.repositories.ReminderPlanPersistenceRepository;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter that implements the reminder plan repository port with Spring Data JPA.
 */
@Repository
public class ReminderPlanRepositoryImpl implements ReminderPlanRepository {

    private final ReminderPlanPersistenceRepository persistenceRepository;

    public ReminderPlanRepositoryImpl(ReminderPlanPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    public Optional<ReminderPlan> findByGroupId(UUID groupId) {
        return persistenceRepository.findByGroupId(groupId).map(ReminderPlanRepositoryImpl::toDomain);
    }

    @Override
    public ReminderPlan save(ReminderPlan plan) {
        var entity = persistenceRepository.findById(plan.getId()).orElseGet(ReminderPlanPersistenceEntity::new);
        entity.setId(plan.getId());
        entity.setGroupId(plan.getGroupId());
        entity.setOffsetsInDays(plan.getOffsetsInDays().stream().map(String::valueOf).collect(Collectors.joining(",")));
        entity.setSendHour(plan.getSendHour());
        entity.setEnabled(plan.isEnabled());
        entity.setUpdatedAt(plan.getUpdatedAt());
        return toDomain(persistenceRepository.save(entity));
    }

    private static ReminderPlan toDomain(ReminderPlanPersistenceEntity entity) {
        var offsets = Arrays.stream(entity.getOffsetsInDays().split(","))
                .filter(value -> !value.isBlank())
                .map(value -> Integer.valueOf(value.trim()))
                .toList();
        var plan = new ReminderPlan();
        plan.restoreState(entity.getId(), entity.getGroupId(), offsets, entity.getSendHour(), entity.isEnabled(),
                entity.getUpdatedAt());
        return plan;
    }
}
