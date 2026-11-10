package pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.GroupCreatedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.GroupDeletedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.GroupFilledEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.GroupStartedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.ManualMemberAddedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.MemberDroppedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.MemberJoinedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.MemberLinkedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.MemberRemovedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.MemberReplacedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.NewCycleOpenedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.RulesUpdatedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.TurnsAssignedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Destination;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupRules;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupStatus;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipKind;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnSlot;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SavingsGroup aggregate root: the savings group with its rules, its members and the collection order.
 *
 * <p>It enforces the conditions to start (every seat taken, turns assigned and a destination for the
 * contributions) and fixes the rules once the group has started. The organizer is also a member and
 * collects a turn like everyone else.</p>
 */
@Getter
public class SavingsGroup extends AbstractDomainAggregateRoot<SavingsGroup> {

    public static final int NAME_MAX_LENGTH = 80;

    private UUID id;
    private String name;
    private UUID organizerId;
    private GroupRules rules;
    private List<Membership> memberships = new ArrayList<>();
    private List<TurnSlot> turns = new ArrayList<>();
    private @Nullable TurnMethod turnMethod;
    private @Nullable String drawSeed;
    private @Nullable Instant turnsAssignedAt;
    private GroupStatus status;
    private Instant createdAt;
    private @Nullable Instant startedAt;

    public SavingsGroup() {
    }

    /**
     * Creates a savings group. The organizer becomes its first member.
     *
     * @param organizerId   the account of the member who creates the group
     * @param organizerName the display name of the organizer
     * @param name          the name of the group
     * @param rules         the initial rules
     * @param now           the current time
     * @return the new group, in DRAFT
     */
    public static SavingsGroup create(UUID organizerId, String organizerName, String name, GroupRules rules,
                                      Instant now) {
        var group = new SavingsGroup();
        group.id = UUID.randomUUID();
        group.name = requireName(name);
        group.organizerId = organizerId;
        group.rules = rules;
        group.status = GroupStatus.DRAFT;
        group.createdAt = now;
        group.memberships.add(Membership.ofAppMember(organizerId, organizerName, now));
        group.registerDomainEvent(new GroupCreatedEvent(group.id, organizerId, group.name, now));
        return group;
    }

    // Queries

    public boolean isOrganizer(UUID accountId) {
        return organizerId.equals(accountId);
    }

    /**
     * Returns true when the account takes part in the group with an active membership.
     */
    public boolean isMember(UUID accountId) {
        return membershipOf(accountId).isPresent();
    }

    public Optional<Membership> membershipOf(UUID accountId) {
        return activeMemberships().stream().filter(membership -> membership.belongsTo(accountId)).findFirst();
    }

    public Optional<Membership> findMembership(UUID membershipId) {
        return memberships.stream().filter(membership -> membership.getId().equals(membershipId)).findFirst();
    }

    public List<Membership> activeMemberships() {
        return memberships.stream().filter(Membership::isActive).toList();
    }

    /**
     * The accounts of the active members who use the application, without the organizer: who hears about
     * what the organizer does.
     */
    public List<UUID> otherMemberAccountIds() {
        return activeMemberships().stream()
                .map(Membership::getMemberId)
                .filter(memberId -> memberId != null && !memberId.equals(organizerId))
                .toList();
    }

    public int activeMembersCount() {
        return activeMemberships().size();
    }

    public int freeSeats() {
        return Math.max(0, rules.seats() - activeMembersCount());
    }

    public boolean hasFreeSeats() {
        return freeSeats() > 0;
    }

    public boolean isFull() {
        return activeMembersCount() == rules.seats();
    }

    public boolean hasTurns() {
        return !turns.isEmpty();
    }

    public boolean isStarted() {
        return status == GroupStatus.STARTED || status == GroupStatus.CLOSED;
    }

    /**
     * The group can start when every seat is taken, the turns are assigned and the
     * destination of the contributions is defined.
     */
    public boolean canStart() {
        return !isStarted() && isFull() && hasTurns() && rules.hasDestination();
    }

    /**
     * Returns the turn of a membership, if the turns are assigned.
     */
    public Optional<TurnSlot> turnOf(UUID membershipId) {
        return turns.stream().filter(turn -> turn.membershipId().equals(membershipId)).findFirst();
    }

    /**
     * Returns the turns in collection order.
     */
    public List<TurnSlot> turnCalendar() {
        return turns.stream().sorted(Comparator.comparingInt(TurnSlot::turnNumber)).toList();
    }

    // Commands

    /**
     * Replaces the rules. Changing the number of members discards the turns, because they no longer
     * cover the whole group.
     */
    public void updateRules(String name, GroupRules newRules, Instant now) {
        requireNotStarted();
        if (newRules.seats() < activeMembersCount()) {
            throw new BusinessRuleViolationException("SEATS_BELOW_MEMBERS",
                    "The group already has %d members".formatted(activeMembersCount()));
        }
        if (newRules.seats() != rules.seats()) {
            clearTurns();
        }
        var newName = requireName(name);
        var changed = !newName.equals(this.name) || !newRules.equals(rules);
        var wasFull = isFull();
        this.name = newName;
        this.rules = newRules;
        refreshReadiness();
        if (changed) {
            registerDomainEvent(new RulesUpdatedEvent(id, now));
        }
        if (!wasFull) {
            announceIfFull(now);
        }
    }

    public void defineDestination(Destination destination, Instant now) {
        requireNotStarted();
        var changed = !destination.equals(rules.destination());
        this.rules = rules.withDestination(destination);
        refreshReadiness();
        if (changed) {
            registerDomainEvent(new RulesUpdatedEvent(id, now));
        }
    }

    /**
     * Adds a member who joined with an invitation code. A member the organizer removed gets the same
     * membership back.
     */
    public Membership join(UUID memberId, String displayName, Instant now) {
        requireNotStarted();
        if (isMember(memberId)) {
            throw new BusinessRuleViolationException("ALREADY_A_MEMBER", "The member already belongs to the group");
        }
        requireFreeSeat();
        var removed = memberships.stream()
                .filter(membership -> membership.belongsTo(memberId) && membership.isRemoved())
                .findFirst();
        Membership membership;
        if (removed.isPresent()) {
            membership = removed.get();
            membership.rejoin(displayName, now);
        } else {
            membership = Membership.ofAppMember(memberId, displayName, now);
            memberships.add(membership);
        }
        registerDomainEvent(new MemberJoinedEvent(id, membership.getId(), memberId, now));
        refreshReadiness();
        announceIfFull(now);
        return membership;
    }

    /**
     * Adds a member who does not use the application; the organizer records that member's contributions.
     */
    /**
     * Links a new account to the member the organizer registered without the application with the same number,
     * so the member keeps the seat, the turn and the contributions recorded so far. Nothing changes when no
     * such member is waiting or when the account already has a membership in the group.
     *
     * @param phone the verified number of the account in E.164 format
     * @return the linked membership, if there was one
     */
    public Optional<Membership> linkManualMember(String phone, UUID accountId, Instant now) {
        if (memberships.stream().anyMatch(membership -> membership.belongsTo(accountId))) {
            return Optional.empty();
        }
        var manual = memberships.stream()
                .filter(membership -> membership.isActive() && membership.isUnlinkedManualMemberWith(phone))
                .findFirst();
        manual.ifPresent(membership -> {
            membership.linkAccount(accountId);
            registerDomainEvent(new MemberLinkedEvent(id, membership.getId(), accountId, now));
        });
        return manual;
    }

    public Membership addManualMember(String displayName, @Nullable String phone, Instant now) {
        requireNotStarted();
        requireFreeSeat();
        var membership = Membership.ofManualMember(displayName, phone, now);
        memberships.add(membership);
        registerDomainEvent(new ManualMemberAddedEvent(id, membership.getId(), membership.getDisplayName(), now));
        refreshReadiness();
        announceIfFull(now);
        return membership;
    }

    /**
     * Removes a member before the group starts. The turns are discarded because they included that member.
     */
    public void removeMember(UUID membershipId, Instant now) {
        requireNotStarted();
        var membership = findMembership(membershipId)
                .filter(Membership::isActive)
                .orElseThrow(() -> new BusinessRuleViolationException("MEMBERSHIP_NOT_ACTIVE",
                        "The member is not active in the group"));
        if (membership.belongsTo(organizerId)) {
            throw new BusinessRuleViolationException("ORGANIZER_CANNOT_BE_REMOVED",
                    "The organizer cannot leave the group");
        }
        membership.remove();
        clearTurns();
        registerDomainEvent(new MemberRemovedEvent(id, membershipId, now));
        refreshReadiness();
    }

    /**
     * Sets the collection order. Every active member gets exactly one turn, numbered from 1.
     *
     * @param slots  the turns
     * @param method how the order was decided
     * @param seed   the seed of the draw, published so anyone can reproduce it; null for other methods
     * @param now    the current time
     */
    public void assignTurns(List<TurnSlot> slots, TurnMethod method, @Nullable String seed, Instant now) {
        requireNotStarted();
        if (!isFull()) {
            throw new BusinessRuleViolationException("SAVINGS_GROUP_NOT_FULL",
                    "Every seat must be taken before assigning the turns");
        }
        requireValidOrder(slots);
        this.turns = new ArrayList<>(slots);
        this.turnMethod = method;
        this.drawSeed = seed;
        this.turnsAssignedAt = now;
        registerDomainEvent(new TurnsAssignedEvent(id, method, now));
        refreshReadiness();
    }

    /**
     * Starts the group: the rules can no longer change and the cycle begins in Contributions.
     */
    public void start(Instant now) {
        requireNotStarted();
        if (!canStart()) {
            throw new BusinessRuleViolationException("SAVINGS_GROUP_NOT_READY",
                    "Every seat must be taken, the turns assigned and the destination defined");
        }
        this.status = GroupStatus.STARTED;
        this.startedAt = now;
        var startedTurns = turnCalendar().stream()
                .map(turn -> {
                    var membership = findMembership(turn.membershipId()).orElseThrow();
                    return new GroupStartedEvent.StartedTurn(turn.turnNumber(), membership.getId(),
                            membership.getMemberId(), membership.getDisplayName(),
                            rules.cutoffDateOfTurn(turn.turnNumber()));
                })
                .toList();
        registerDomainEvent(new GroupStartedEvent(id, organizerId, rules, startedTurns, now));
    }

    /**
     * Deletes the group before it starts. Once it has started, its history belongs to every member.
     */
    public void delete(Instant now) {
        requireNotStarted();
        registerDomainEvent(new GroupDeletedEvent(id, name, organizerId, otherMemberAccountIds(), now));
    }

    /**
     * Closes the group when its cycle is over. Closing a closed group changes nothing.
     */
    public void close() {
        if (status == GroupStatus.CLOSED) {
            return;
        }
        if (status != GroupStatus.STARTED) {
            throw new BusinessRuleViolationException("SAVINGS_GROUP_NOT_STARTED",
                    "Only a group whose cycle has started can be closed");
        }
        this.status = GroupStatus.CLOSED;
    }

    /**
     * Gives two members the turns Contributions swapped after the group started, so the calendar of the group
     * shows who collects in each turn.
     */
    public void applySwappedTurns(UUID firstMembershipId, int firstTurn, UUID secondMembershipId, int secondTurn) {
        for (var index = 0; index < turns.size(); index++) {
            var turn = turns.get(index);
            if (turn.membershipId().equals(firstMembershipId)) {
                turns.set(index, new TurnSlot(firstTurn, firstMembershipId, turn.assignedBy()));
            } else if (turn.membershipId().equals(secondMembershipId)) {
                turns.set(index, new TurnSlot(secondTurn, secondMembershipId, turn.assignedBy()));
            }
        }
    }

    /**
     * Records that a member left the group after it started. The member keeps the contributions and the pot
     * received so far; their turn waits for a replacement.
     */
    public Membership registerDropout(UUID membershipId, Instant now) {
        if (status != GroupStatus.STARTED) {
            throw new BusinessRuleViolationException("SAVINGS_GROUP_NOT_STARTED",
                    "Only a member of a group in progress can drop out; before it starts, remove the member");
        }
        var membership = findMembership(membershipId).filter(Membership::isActive)
                .orElseThrow(() -> new BusinessRuleViolationException("MEMBER_NOT_ACTIVE",
                        "The member is not active in the group"));
        if (membership.belongsTo(organizerId)) {
            throw new BusinessRuleViolationException("ORGANIZER_CANNOT_DROP_OUT",
                    "The organizer cannot drop out of their own group");
        }
        membership.markDropped();
        registerDomainEvent(new MemberDroppedEvent(id, membershipId, membership.getMemberId(),
                membership.getDisplayName(), now));
        return membership;
    }

    /**
     * Gives the seat and the turn of a member who dropped out to someone else.
     *
     * @param accountId the account of the replacement, or null when they do not use the application
     * @return the membership of the replacement
     */
    public Membership replaceDroppedMember(UUID droppedMembershipId, @Nullable UUID accountId, String displayName,
                                           @Nullable String phone, Instant now) {
        if (status != GroupStatus.STARTED) {
            throw new BusinessRuleViolationException("SAVINGS_GROUP_NOT_STARTED",
                    "Only a member of a group in progress can be replaced");
        }
        var dropped = findMembership(droppedMembershipId).filter(Membership::isDropped)
                .orElseThrow(() -> new BusinessRuleViolationException("MEMBER_NOT_DROPPED",
                        "Only a member who dropped out can be replaced"));
        var turnIndex = -1;
        for (var index = 0; index < turns.size(); index++) {
            if (turns.get(index).membershipId().equals(dropped.getId())) {
                turnIndex = index;
            }
        }
        if (turnIndex < 0) {
            throw new BusinessRuleViolationException("MEMBER_ALREADY_REPLACED", "The member was already replaced");
        }
        if (accountId != null && memberships.stream().anyMatch(membership -> membership.belongsTo(accountId))) {
            throw new BusinessRuleViolationException("ALREADY_A_MEMBER", "The member already belongs to the group");
        }
        var replacement = Membership.ofReplacement(accountId, displayName, phone, now);
        memberships.add(replacement);
        var turn = turns.get(turnIndex);
        turns.set(turnIndex, new TurnSlot(turn.turnNumber(), replacement.getId(), turn.assignedBy()));
        registerDomainEvent(new MemberReplacedEvent(id, droppedMembershipId, replacement.getId(), accountId,
                replacement.getDisplayName(), turn.turnNumber(), now));
        return replacement;
    }

    /**
     * Returns the cutoff date of a turn.
     */
    public LocalDate cutoffDateOfTurn(int turnNumber) {
        return rules.cutoffDateOfTurn(turnNumber);
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, String name, UUID organizerId, GroupRules rules, List<Membership> memberships,
                             List<TurnSlot> turns, @Nullable TurnMethod turnMethod, @Nullable String drawSeed,
                             @Nullable Instant turnsAssignedAt, GroupStatus status, Instant createdAt,
                             @Nullable Instant startedAt) {
        this.id = id;
        this.name = name;
        this.organizerId = organizerId;
        this.rules = rules;
        this.memberships = new ArrayList<>(memberships);
        this.turns = new ArrayList<>(turns);
        this.turnMethod = turnMethod;
        this.drawSeed = drawSeed;
        this.turnsAssignedAt = turnsAssignedAt;
        this.status = status;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
    }

    public List<Membership> getMemberships() {
        return Collections.unmodifiableList(memberships);
    }

    public List<TurnSlot> getTurns() {
        return Collections.unmodifiableList(turns);
    }

    // Invariants

    private void requireNotStarted() {
        if (isStarted()) {
            throw new BusinessRuleViolationException("SAVINGS_GROUP_ALREADY_STARTED",
                    "The group has already started and its setup can no longer change");
        }
    }

    private void requireFreeSeat() {
        if (!hasFreeSeats()) {
            throw new BusinessRuleViolationException("SAVINGS_GROUP_FULL", "The group has no free seats");
        }
    }

    private void requireValidOrder(List<TurnSlot> slots) {
        var activeIds = new HashSet<UUID>();
        activeMemberships().forEach(membership -> activeIds.add(membership.getId()));
        var assignedIds = new HashSet<UUID>();
        var numbers = new HashSet<Integer>();
        for (var slot : slots) {
            assignedIds.add(slot.membershipId());
            numbers.add(slot.turnNumber());
        }
        var everyMemberOnce = slots.size() == activeIds.size() && assignedIds.equals(activeIds);
        var consecutiveNumbers = numbers.size() == slots.size()
                && numbers.stream().allMatch(number -> number >= 1 && number <= slots.size());
        if (!everyMemberOnce || !consecutiveNumbers) {
            throw new BusinessRuleViolationException("INVALID_TURN_ORDER",
                    "Every member must get exactly one turn, numbered from 1 to the number of members");
        }
    }

    private void announceIfFull(Instant now) {
        if (isFull()) {
            registerDomainEvent(new GroupFilledEvent(id, now));
        }
    }

    private void clearTurns() {
        turns.clear();
        turnMethod = null;
        drawSeed = null;
        turnsAssignedAt = null;
    }

    /**
     * Opens the next cycle of a group whose cycle is over: a new group, still to start, with the same name,
     * rules, destination and active members, so the organizer only reviews it, assigns the turns and starts it.
     *
     * @param firstContributionDate the cutoff of the first period of the new cycle
     * @return the new group
     */
    public SavingsGroup openNewCycle(LocalDate firstContributionDate, Instant now) {
        if (status != GroupStatus.CLOSED) {
            throw new BusinessRuleViolationException("SAVINGS_GROUP_NOT_CLOSED",
                    "A new cycle can only be opened once the group completed its cycle");
        }
        var organizerName = membershipOf(organizerId).map(Membership::getDisplayName).orElse(name);
        var nextRules = new GroupRules(rules.contribution(), rules.periodicity(), rules.seats(), firstContributionDate,
                rules.destination());
        var next = create(organizerId, organizerName, name, nextRules, now);
        activeMemberships().stream()
                .filter(membership -> !membership.belongsTo(organizerId))
                .forEach(membership -> next.memberships.add(membership.getKind() == MembershipKind.APP
                        ? Membership.ofAppMember(membership.getMemberId(), membership.getDisplayName(), now)
                        : Membership.ofManualMember(membership.getDisplayName(), membership.getPhone(), now)));
        next.refreshReadiness();
        next.registerDomainEvent(new NewCycleOpenedEvent(next.id, id, next.name, next.otherMemberAccountIds(), now));
        return next;
    }

    private void refreshReadiness() {
        if (!isStarted()) {
            status = canStart() ? GroupStatus.READY : GroupStatus.DRAFT;
        }
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The name of the group is required");
        }
        var stripped = name.strip();
        if (stripped.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "The name of the group cannot exceed %d characters".formatted(NAME_MAX_LENGTH));
        }
        return stripped;
    }
}
