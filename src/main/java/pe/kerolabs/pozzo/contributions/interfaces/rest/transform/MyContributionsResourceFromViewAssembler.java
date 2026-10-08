package pe.kerolabs.pozzo.contributions.interfaces.rest.transform;

import pe.kerolabs.pozzo.contributions.application.queryservices.PeriodView;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.MyContributionsResource;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Converts the periods of a cycle, with the requester's contributions, into {@link MyContributionsResource}.
 */
public class MyContributionsResourceFromViewAssembler {

    public static MyContributionsResource toResourceFromViews(Cycle cycle, List<PeriodView> views,
                                                              UUID requesterAccountId, LocalDate today) {
        var membershipId = cycle.participantOf(requesterAccountId).orElseThrow().membershipId();
        var currency = cycle.getRules().contribution().currency();
        var contributed = Money.zero(currency);
        var pending = Money.zero(currency);
        for (var view : views) {
            var expected = view.period().expectedFor(membershipId).orElseThrow();
            if (expected.isPending()) {
                pending = pending.plus(expected.getAmount());
            } else {
                contributed = contributed.plus(expected.getAmount());
            }
        }
        var periods = views.stream()
                .sorted(Comparator.comparingInt((PeriodView view) -> view.period().getTurnNumber()).reversed())
                .map(view -> new MyContributionsResource.PeriodItem(
                        view.period().getId(),
                        view.period().getTurnNumber(),
                        view.period().getCutoffDate(),
                        view.period().expectedFor(membershipId).orElseThrow().getAmount().amount(),
                        PeriodStatusResourceFromViewAssembler.statusOf(view, membershipId, today),
                        view.contributions().stream().findFirst()
                                .map(contribution -> ContributionResourceFromEntityAssembler.toResourceFromEntity(
                                        contribution, cycle))
                                .orElse(null)))
                .toList();
        return new MyContributionsResource(cycle.getId(), cycle.getGroupName(), contributed.amount(),
                pending.amount(), periods);
    }
}
