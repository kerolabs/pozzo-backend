package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.PaymentMethod;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Periodicity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to create a savings group. The requester becomes its organizer.
 */
public record CreateGroupCommand(UUID organizerId, String name, BigDecimal contributionAmount, Periodicity periodicity, int seats,
                                 LocalDate firstContributionDate, @Nullable PaymentMethod destinationMethod,
                                 @Nullable String destinationPhoneNumber) {
}
