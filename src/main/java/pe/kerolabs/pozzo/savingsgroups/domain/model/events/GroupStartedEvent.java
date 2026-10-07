package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupRules;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The organizer started the group: its rules are fixed and the cycle begins.
 * Contributions consumes it to open the cycle, so it carries a copy of the rules and the turns.
 *
 * @param groupId     the savings group
 * @param organizerId the account of the organizer
 * @param rules       the rules, which can no longer change
 * @param turns       the collection order with the cutoff date of each turn
 * @param occurredAt  the moment the group started
 */
public record GroupStartedEvent(UUID groupId, UUID organizerId, GroupRules rules, List<StartedTurn> turns,
                                Instant occurredAt) {

    /**
     * A turn of the started group.
     *
     * @param turnNumber   the position, starting at 1
     * @param membershipId the membership that collects
     * @param memberId     the account of the member, or null for a member without the application
     * @param displayName  the name of the member
     * @param cutoffDate   the date contributions of that period are due
     */
    public record StartedTurn(int turnNumber, UUID membershipId, @Nullable UUID memberId, String displayName,
                              LocalDate cutoffDate) {
    }
}
