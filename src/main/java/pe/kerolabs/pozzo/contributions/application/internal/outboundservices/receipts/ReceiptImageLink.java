package pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts;

import java.time.Instant;

/**
 * A temporary link to the image of a receipt.
 *
 * @param url       the signed URL of the image
 * @param expiresAt when the link stops working
 */
public record ReceiptImageLink(String url, Instant expiresAt) {
}
