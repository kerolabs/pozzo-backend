package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Embeddable persistence representation of a turn of the cycle, stored in {@code cycle_turns}.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CycleTurnEmbeddable {

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;
}
