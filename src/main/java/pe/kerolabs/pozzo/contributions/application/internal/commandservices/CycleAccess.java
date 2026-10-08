package pe.kerolabs.pozzo.contributions.application.internal.commandservices;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.repositories.CycleRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.PeriodRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.util.UUID;

/**
 * Who may act on a period. A requester who does not take part in the cycle gets "not found", the
 * same answer as for a period that does not exist; a member who is not the organizer gets
 * "forbidden" for the operations reserved to the organizer.
 */
final class CycleAccess {

    private CycleAccess() {
    }

    record PeriodInCycle(Cycle cycle, Period period) {
    }

    static Result<PeriodInCycle, ApplicationError> requireParticipant(CycleRepository cycleRepository,
                                                                     PeriodRepository periodRepository,
                                                                     UUID periodId, UUID requesterId) {
        var period = periodRepository.findById(periodId);
        var cycle = period.flatMap(found -> cycleRepository.findById(found.getCycleId()))
                .filter(found -> found.isParticipant(requesterId));
        if (period.isEmpty() || cycle.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Period", periodId.toString()));
        }
        return Result.success(new PeriodInCycle(cycle.get(), period.get()));
    }

    static Result<PeriodInCycle, ApplicationError> requireOrganizer(CycleRepository cycleRepository,
                                                                   PeriodRepository periodRepository,
                                                                   UUID periodId, UUID requesterId) {
        return requireParticipant(cycleRepository, periodRepository, periodId, requesterId)
                .flatMap(access -> access.cycle().isOrganizer(requesterId)
                        ? Result.success(access)
                        : Result.failure(ApplicationError.forbidden(
                                "ONLY_ORGANIZER", "Only the organizer of the group can do this")));
    }
}
