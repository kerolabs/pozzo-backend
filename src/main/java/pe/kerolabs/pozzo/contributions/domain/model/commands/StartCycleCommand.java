package pe.kerolabs.pozzo.contributions.domain.model.commands;

import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Periodicity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Command to start the cycle of a savings group that has just started, with a copy of its rules
 * and its collection order.
 */
public record StartCycleCommand(UUID groupId, String groupName, UUID organizerAccountId, String organizerName,
                                BigDecimal contributionAmount, String currency, Periodicity periodicity,
                                LocalDate firstContributionDate, String destinationMethod, String destinationPhone,
                                List<CycleTurn> turns) {
}
