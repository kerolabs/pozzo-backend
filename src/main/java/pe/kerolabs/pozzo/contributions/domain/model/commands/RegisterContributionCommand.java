package pe.kerolabs.pozzo.contributions.domain.model.commands;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReceiptSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to register a contribution with the data read from its receipt and confirmed by the member.
 */
public record RegisterContributionCommand(UUID periodId, UUID requesterAccountId, String operationNumber,
                                          @Nullable String payerName, String payeeName, BigDecimal amount,
                                          LocalDate paidAt, ReceiptSource source) {
}
