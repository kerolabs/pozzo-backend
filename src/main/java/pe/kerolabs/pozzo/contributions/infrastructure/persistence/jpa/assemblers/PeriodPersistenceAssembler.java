package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.model.entities.ExpectedContribution;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.ExpectedContributionPersistenceEntity;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.PeriodPersistenceEntity;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Static assembler between the period aggregate and its persistence entities.
 */
public final class PeriodPersistenceAssembler {

    private PeriodPersistenceAssembler() {
    }

    public static Period toDomainFromPersistence(PeriodPersistenceEntity entity) {
        var expected = entity.getExpected().stream()
                .map(item -> {
                    var domain = new ExpectedContribution();
                    domain.restoreState(item.getId(), item.getMembershipId(),
                            Money.of(item.getAmount(), item.getCurrency()), item.getStatus(),
                            item.getSettledByContribution());
                    return domain;
                })
                .toList();
        var period = new Period();
        period.restoreState(entity.getId(), entity.getCycleId(), entity.getTurnNumber(), entity.getOpensAt(),
                entity.getCutoffDate(), entity.getPayoutMembershipId(), expected, entity.getStatus(),
                entity.getDeliveredAt(), entity.getDeliveredBy());
        return period;
    }

    /**
     * Copies the state of the aggregate onto a new or already loaded persistence entity.
     * Expected contributions are matched by id, so existing rows are updated instead of replaced.
     */
    public static PeriodPersistenceEntity toPersistenceFromDomain(Period period, PeriodPersistenceEntity entity) {
        entity.setId(period.getId());
        entity.setCycleId(period.getCycleId());
        entity.setTurnNumber(period.getTurnNumber());
        entity.setOpensAt(period.getOpensAt());
        entity.setCutoffDate(period.getCutoffDate());
        entity.setPayoutMembershipId(period.getPayoutMembershipId());
        entity.setStatus(period.getStatus());
        entity.setDeliveredAt(period.getDeliveredAt());
        entity.setDeliveredBy(period.getDeliveredBy());
        var existing = entity.getExpected().stream()
                .collect(Collectors.toMap(ExpectedContributionPersistenceEntity::getId, Function.identity()));
        for (var item : period.getExpected()) {
            var itemEntity = existing.get(item.getId());
            if (itemEntity == null) {
                itemEntity = new ExpectedContributionPersistenceEntity();
                itemEntity.setPeriod(entity);
                entity.getExpected().add(itemEntity);
            }
            itemEntity.setId(item.getId());
            itemEntity.setMembershipId(item.getMembershipId());
            itemEntity.setAmount(item.getAmount().amount());
            itemEntity.setCurrency(item.getAmount().currency());
            itemEntity.setStatus(item.getStatus());
            itemEntity.setSettledByContribution(item.getSettledByContributionId());
        }
        return entity;
    }
}
