package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Destination;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupRules;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnSlot;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.embeddables.TurnSlotEmbeddable;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities.MembershipPersistenceEntity;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities.SavingsGroupPersistenceEntity;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Static assembler between the savings group aggregate and its persistence entities.
 */
public final class SavingsGroupPersistenceAssembler {

    private SavingsGroupPersistenceAssembler() {
    }

    public static SavingsGroup toDomainFromPersistence(SavingsGroupPersistenceEntity entity) {
        var destination = entity.getDestinationMethod() == null
                ? null
                : new Destination(entity.getDestinationMethod(), entity.getDestinationPhone());
        var rules = new GroupRules(
                new Money(entity.getContributionAmount(), entity.getCurrency()),
                entity.getPeriodicity(),
                entity.getSeats(),
                entity.getFirstContributionDate(),
                destination);
        var memberships = entity.getMemberships().stream()
                .map(SavingsGroupPersistenceAssembler::toDomainFromPersistence)
                .toList();
        var turns = entity.getTurns().stream()
                .map(turn -> new TurnSlot(turn.getTurnNumber(), turn.getMembershipId(), turn.getAssignedBy()))
                .toList();
        var group = new SavingsGroup();
        group.restoreState(
                entity.getId(),
                entity.getName(),
                entity.getOrganizerId(),
                rules,
                memberships,
                turns,
                entity.getTurnMethod(),
                entity.getDrawSeed(),
                entity.getTurnsAssignedAt(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getStartedAt());
        return group;
    }

    /**
     * Copies the state of the aggregate onto a new or already loaded persistence entity.
     * Memberships are matched by id, so existing rows are updated instead of replaced.
     */
    public static SavingsGroupPersistenceEntity toPersistenceFromDomain(SavingsGroup group,
                                                                        SavingsGroupPersistenceEntity entity) {
        var rules = group.getRules();
        entity.setId(group.getId());
        entity.setName(group.getName());
        entity.setOrganizerId(group.getOrganizerId());
        entity.setContributionAmount(rules.contribution().amount());
        entity.setCurrency(rules.contribution().currency());
        entity.setPeriodicity(rules.periodicity());
        entity.setCutoffDay(rules.cutoffDay());
        entity.setSeats(rules.seats());
        entity.setFirstContributionDate(rules.firstContributionDate());
        entity.setDestinationMethod(rules.hasDestination() ? rules.destination().method() : null);
        entity.setDestinationPhone(rules.hasDestination() ? rules.destination().phoneNumber() : null);
        entity.setTurnMethod(group.getTurnMethod());
        entity.setDrawSeed(group.getDrawSeed());
        entity.setTurnsAssignedAt(group.getTurnsAssignedAt());
        entity.setStatus(group.getStatus());
        entity.setCreatedAt(group.getCreatedAt());
        entity.setStartedAt(group.getStartedAt());

        var existing = entity.getMemberships().stream()
                .collect(Collectors.toMap(MembershipPersistenceEntity::getId, Function.identity()));
        for (var membership : group.getMemberships()) {
            var membershipEntity = existing.get(membership.getId());
            if (membershipEntity == null) {
                membershipEntity = new MembershipPersistenceEntity();
                membershipEntity.setGroup(entity);
                entity.getMemberships().add(membershipEntity);
            }
            copyMembership(membership, membershipEntity);
        }

        entity.getTurns().clear();
        group.getTurns().forEach(turn -> entity.getTurns().add(
                new TurnSlotEmbeddable(turn.turnNumber(), turn.membershipId(), turn.assignedBy())));
        return entity;
    }

    private static Membership toDomainFromPersistence(MembershipPersistenceEntity entity) {
        var membership = new Membership();
        membership.restoreState(
                entity.getId(),
                entity.getMemberId(),
                entity.getDisplayName(),
                entity.getPhone(),
                entity.getKind(),
                entity.getStatus(),
                entity.getJoinedAt());
        return membership;
    }

    private static void copyMembership(Membership membership, MembershipPersistenceEntity entity) {
        entity.setId(membership.getId());
        entity.setMemberId(membership.getMemberId());
        entity.setDisplayName(membership.getDisplayName());
        entity.setPhone(membership.getPhone());
        entity.setKind(membership.getKind());
        entity.setStatus(membership.getStatus());
        entity.setJoinedAt(membership.getJoinedAt());
    }
}
