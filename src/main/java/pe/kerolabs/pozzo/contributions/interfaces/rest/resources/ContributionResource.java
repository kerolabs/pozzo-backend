package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionMethod;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReceiptSource;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReviewDecision;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A contribution with its receipt, its validation and its review.
 */
@Schema(name = "Contribution", description = "A contribution with its receipt, validation and review")
public record ContributionResource(
        @Schema(description = "Contribution identifier") UUID id,
        @Schema(description = "Period identifier") UUID periodId,
        @Schema(description = "Membership of the member it counts for") UUID membershipId,
        @Schema(description = "Name of that member", example = "Marta Quispe") String memberName,
        @Schema(description = "Amount", example = "300.00") BigDecimal amount,
        @Schema(description = "TRANSFER, CASH or COVERAGE") ContributionMethod method,
        @Schema(description = "VALIDATED, INCONSISTENT, APPROVED or REJECTED") ContributionStatus status,
        @Schema(description = "Data of the receipt, for transfers", nullable = true) Receipt receipt,
        @Schema(description = "Fields of the receipt that did not match") List<InconsistencyItem> inconsistencies,
        @Schema(description = "Member who covered it, for coverages", nullable = true) UUID coveredByMembershipId,
        @Schema(description = "Decision of the organizer, if reviewed", nullable = true) ReviewItem review,
        @Schema(description = "When it was registered") Instant registeredAt,
        @Schema(description = "True when the image of the receipt was kept; ask for its link to see it")
        boolean hasReceiptImage) {

    @Schema(name = "Receipt", description = "Data of the receipt")
    public record Receipt(String operationNumber, String payerName, String payeeName, BigDecimal amount,
                          LocalDate paidAt, ReceiptSource source) {
    }

    @Schema(name = "Inconsistency", description = "A field of the receipt that did not match")
    public record InconsistencyItem(
            @Schema(description = "AMOUNT, PAYEE or DATE", example = "AMOUNT") String field,
            @Schema(description = "What was expected", example = "S/ 300.00") String expected,
            @Schema(description = "What the receipt shows", example = "S/ 280.00") String found) {
    }

    @Schema(name = "Review", description = "Decision of the organizer")
    public record ReviewItem(ReviewDecision decision, String note, Instant reviewedAt) {
    }
}
