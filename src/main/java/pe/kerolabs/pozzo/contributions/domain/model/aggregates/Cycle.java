package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.contributions.domain.model.events.CycleClosedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.CycleStartedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleRules;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cycle aggregate root: the frozen copy of a started savings group that Contributions works with.
 *
 * <p>It is created when Savings Groups announces that a group started and from then on does not
 * depend on that context: it keeps the rules, the collection order and the turn in progress, and
 * decides when the cycle is over.</p>
 */
@Getter
public class Cycle extends AbstractDomainAggregateRoot<Cycle> {

    private UUID id;
    private UUID groupId;
    private String groupName;
    private UUID organizerAccountId;
    private CycleRules rules;
    private List<CycleTurn> turns = new ArrayList<>();
    private int currentTurn;
    private CycleStatus status;
    private Instant startedAt;
    private @Nullable Instant closedAt;

    public Cycle() {
    }

    /**
     * Starts the cycle of a group. The first turn is the one in progress.
     */
    public static Cycle start(UUID groupId, String groupName, UUID organizerAccountId, CycleRules rules,
                              List<CycleTurn> turns, Instant now) {
        if (turns.size() < 2) {
            throw new IllegalArgumentException("A cycle needs at least two members");
        }
        var cycle = new Cycle();
        cycle.id = UUID.randomUUID();
        cycle.groupId = groupId;
        cycle.groupName = groupName;
        cycle.organizerAccountId = organizerAccountId;
        cycle.rules = rules;
        cycle.turns = new ArrayList<>(turns.stream().sorted(Comparator.comparingInt(CycleTurn::turnNumber)).toList());
        cycle.currentTurn = 1;
        cycle.status = CycleStatus.ACTIVE;
        cycle.startedAt = now;
        cycle.registerDomainEvent(new CycleStartedEvent(cycle.id, groupId, now));
        return cycle;
    }

    public int totalTurns() {
        return turns.size();
    }

    public boolean isActive() {
        return status == CycleStatus.ACTIVE;
    }

    public boolean isOrganizer(UUID accountId) {
        return organizerAccountId.equals(accountId);
    }

    /**
     * Returns the turn of the member who uses this account, if that member takes part in the cycle.
     */
    public Optional<CycleTurn> participantOf(UUID accountId) {
        return turns.stream().filter(turn -> turn.belongsTo(accountId)).findFirst();
    }

    public boolean isParticipant(UUID accountId) {
        return participantOf(accountId).isPresent();
    }

    public Optional<CycleTurn> findParticipant(UUID membershipId) {
        return turns.stream().filter(turn -> turn.membershipId().equals(membershipId)).findFirst();
    }

    public CycleTurn turn(int turnNumber) {
        return turns.stream().filter(turn -> turn.turnNumber() == turnNumber).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The cycle has no turn " + turnNumber));
    }

    public boolean hasRemainingTurns() {
        return currentTurn < totalTurns();
    }

    /**
     * Moves to the next turn after the pot of the current one was delivered.
     */
    public int advanceToNextTurn() {
        if (!hasRemainingTurns()) {
            throw new BusinessRuleViolationException("NO_REMAINING_TURNS", "Every member has already collected");
        }
        currentTurn++;
        return currentTurn;
    }

    /**
     * Closes the cycle once every member has collected.
     */
    public void close(Instant now) {
        if (hasRemainingTurns()) {
            throw new BusinessRuleViolationException("CYCLE_HAS_REMAINING_TURNS",
                    "The cycle can only close after the last turn");
        }
        this.status = CycleStatus.CLOSED;
        this.closedAt = now;
        registerDomainEvent(new CycleClosedEvent(id, groupId, now));
    }

    public List<CycleTurn> getTurns() {
        return Collections.unmodifiableList(turns);
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID groupId, String groupName, UUID organizerAccountId, CycleRules rules,
                             List<CycleTurn> turns, int currentTurn, CycleStatus status, Instant startedAt,
                             @Nullable Instant closedAt) {
        this.id = id;
        this.groupId = groupId;
        this.groupName = groupName;
        this.organizerAccountId = organizerAccountId;
        this.rules = rules;
        this.turns = new ArrayList<>(turns.stream().sorted(Comparator.comparingInt(CycleTurn::turnNumber)).toList());
        this.currentTurn = currentTurn;
        this.status = status;
        this.startedAt = startedAt;
        this.closedAt = closedAt;
    }
}
