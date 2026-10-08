package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleRules;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables.CycleTurnEmbeddable;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.CyclePersistenceEntity;

/**
 * Static assembler between the cycle aggregate and its persistence entity.
 */
public final class CyclePersistenceAssembler {

    private CyclePersistenceAssembler() {
    }

    public static Cycle toDomainFromPersistence(CyclePersistenceEntity entity) {
        var rules = new CycleRules(
                Money.of(entity.getContributionAmount(), entity.getCurrency()),
                entity.getPeriodicity(),
                entity.getFirstContributionDate(),
                entity.getDestinationMethod(),
                entity.getDestinationPhone(),
                entity.getPayeeName());
        var turns = entity.getTurns().stream()
                .map(turn -> new CycleTurn(turn.getTurnNumber(), turn.getMembershipId(), turn.getAccountId(),
                        turn.getDisplayName()))
                .toList();
        var cycle = new Cycle();
        cycle.restoreState(entity.getId(), entity.getGroupId(), entity.getGroupName(), entity.getOrganizerAccountId(),
                rules, turns, entity.getCurrentTurn(), entity.getStatus(), entity.getStartedAt(), entity.getClosedAt());
        return cycle;
    }

    /**
     * Copies the state of the aggregate onto a new or already loaded persistence entity.
     */
    public static CyclePersistenceEntity toPersistenceFromDomain(Cycle cycle, CyclePersistenceEntity entity) {
        var rules = cycle.getRules();
        entity.setId(cycle.getId());
        entity.setGroupId(cycle.getGroupId());
        entity.setGroupName(cycle.getGroupName());
        entity.setOrganizerAccountId(cycle.getOrganizerAccountId());
        entity.setContributionAmount(rules.contribution().amount());
        entity.setCurrency(rules.contribution().currency());
        entity.setPeriodicity(rules.periodicity());
        entity.setCutoffDay(rules.cutoffDay());
        entity.setFirstContributionDate(rules.firstContributionDate());
        entity.setDestinationMethod(rules.destinationMethod());
        entity.setDestinationPhone(rules.destinationPhone());
        entity.setPayeeName(rules.payeeName());
        entity.setTotalTurns(cycle.totalTurns());
        entity.setCurrentTurn(cycle.getCurrentTurn());
        entity.setStatus(cycle.getStatus());
        entity.setStartedAt(cycle.getStartedAt());
        entity.setClosedAt(cycle.getClosedAt());
        entity.getTurns().clear();
        cycle.getTurns().forEach(turn -> entity.getTurns().add(new CycleTurnEmbeddable(
                turn.turnNumber(), turn.membershipId(), turn.accountId(), turn.displayName())));
        return entity;
    }
}
