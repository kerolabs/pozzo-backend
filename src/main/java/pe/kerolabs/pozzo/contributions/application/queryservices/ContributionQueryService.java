package pe.kerolabs.pozzo.contributions.application.queryservices;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetCurrentPeriodQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetCycleByGroupIdQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetMemberContributionsQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetPendingReviewsQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetPeriodsQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for the read side of Contributions. Every query returns empty when
 * the requester does not take part in the cycle, exactly as when it does not exist.
 */
public interface ContributionQueryService {

    Optional<Cycle> handle(GetCycleByGroupIdQuery query);

    /**
     * The period in progress with the state of every contribution (the pot screen).
     */
    Optional<PeriodView> handle(GetCurrentPeriodQuery query);

    /**
     * Every period opened so far, in turn order (the history screen).
     */
    Optional<List<PeriodView>> handle(GetPeriodsQuery query);

    /**
     * The periods of the cycle with the requester's own contributions (the "my contributions" screen).
     */
    Optional<List<PeriodView>> handle(GetMemberContributionsQuery query);

    /**
     * The contributions of a period that wait for review; only for the organizer.
     */
    Optional<PeriodView> handle(GetPendingReviewsQuery query);
}
