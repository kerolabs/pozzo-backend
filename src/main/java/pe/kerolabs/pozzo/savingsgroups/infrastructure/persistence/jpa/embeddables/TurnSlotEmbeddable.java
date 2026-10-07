package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;

import java.util.UUID;

/**
 * Embeddable persistence representation of a turn, stored in the {@code turn_slots} table.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TurnSlotEmbeddable {

    @Column(name = "turn_number", nullable = false)
    private int turnNumber;

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_by", nullable = false, length = 10)
    private TurnMethod assignedBy;
}
