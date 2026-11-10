package pe.kerolabs.pozzo.contributions.application.queryservices;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;

/**
 * A shared contribution with the cycle and the period it belongs to.
 */
public record VerifiedContributionView(Contribution contribution, Cycle cycle, Period period) {
}
