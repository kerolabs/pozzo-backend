package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Integration event that Savings Groups publishes for the other contexts when a group starts.
 * It carries only primitive values, so the consumers never depend on the Savings Groups model.
 *
 * @param groupId               the savings group
 * @param groupName             the name of the group
 * @param organizerAccountId    the account of the organizer
 * @param organizerName         the name of the organizer, who usually receives the contributions
 * @param contributionAmount    the amount each member contributes every period
 * @param currency              the ISO 4217 currency
 * @param periodicity           WEEKLY, BIWEEKLY or MONTHLY
 * @param firstContributionDate the cutoff date of the first period
 * @param destinationMethod     YAPE or PLIN
 * @param destinationPhone      the wallet number in E.164 format
 * @param turns                 the collection order
 * @param startedAt             the moment the group started
 */
public record SavingsGroupStartedIntegrationEvent(UUID groupId, String groupName, UUID organizerAccountId,
                                                  String organizerName, BigDecimal contributionAmount,
                                                  String currency, String periodicity,
                                                  LocalDate firstContributionDate, String destinationMethod,
                                                  String destinationPhone, List<Turn> turns, Instant startedAt) {

    /**
     * A turn of the started group.
     *
     * @param turnNumber   the position, starting at 1
     * @param membershipId the membership that collects in that turn
     * @param accountId    the account of the member, or null for a member without the application
     * @param displayName  the name of the member
     */
    public record Turn(int turnNumber, UUID membershipId, @Nullable UUID accountId, String displayName) {
    }
}
