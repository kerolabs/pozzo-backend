package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Embeddable persistence representation of an inconsistency, stored in {@code contribution_inconsistencies}.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InconsistencyEmbeddable {

    @Column(name = "field", nullable = false, length = 40)
    private String field;

    @Column(name = "expected_value", nullable = false, length = 120)
    private String expectedValue;

    @Column(name = "found_value", nullable = false, length = 120)
    private String foundValue;
}
