package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A bid of an auction, stored with the auction.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BidEmbeddable {

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;
}
