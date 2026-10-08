package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * A temporary link to the image of a receipt.
 */
@Schema(name = "ReceiptImage", description = "Temporary link to the image of a receipt")
public record ReceiptImageResource(
        @Schema(description = "Signed URL of the image") String url,
        @Schema(description = "When the link stops working") Instant expiresAt) {
}
