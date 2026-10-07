package pe.kerolabs.pozzo.contributions.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.contributions.application.commandservices.CycleCommandService;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.model.commands.DeliverPotCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.StartCycleCommand;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleRules;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.repositories.CycleRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.PeriodRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;

/**
 * Starts cycles and moves them from one turn to the next as each pot is delivered.
 */
@Service
@Transactional
public class CycleCommandServiceImpl implements CycleCommandService {

    private final CycleRepository cycleRepository;
    private final PeriodRepository periodRepository;
    private final Clock clock;

    public CycleCommandServiceImpl(CycleRepository cycleRepository, PeriodRepository periodRepository, Clock clock) {
        this.cycleRepository = cycleRepository;
        this.periodRepository = periodRepository;
        this.clock = clock;
    }

    @Override
    public Result<Cycle, ApplicationError> handle(StartCycleCommand command) {
        var existing = cycleRepository.findByGroupId(command.groupId());
        if (existing.isPresent()) {
            return Result.success(existing.get());
        }
        var now = clock.instant();
        var rules = new CycleRules(Money.of(command.contributionAmount(), command.currency()), command.periodicity(),
                command.firstContributionDate(), command.destinationMethod(), command.destinationPhone(),
                command.organizerName());
        var cycle = Cycle.start(command.groupId(), command.groupName(), command.organizerAccountId(), rules,
                command.turns(), now);
        var saved = cycleRepository.save(cycle);
        periodRepository.save(Period.open(saved, 1, now));
        return Result.success(saved);
    }

    @Override
    public Result<PotDelivery, ApplicationError> handle(DeliverPotCommand command) {
        var now = clock.instant();
        return CycleAccess.requireOrganizer(cycleRepository, periodRepository, command.periodId(),
                        command.requesterAccountId())
                .map(access -> {
                    var cycle = access.cycle();
                    var period = access.period();
                    period.deliverPot(command.requesterAccountId(), now);
                    var delivered = periodRepository.save(period);
                    Period next = null;
                    if (cycle.hasRemainingTurns()) {
                        var nextTurn = cycle.advanceToNextTurn();
                        next = Period.open(cycle, nextTurn, now);
                    } else {
                        cycle.close(now);
                    }
                    var savedCycle = cycleRepository.save(cycle);
                    var savedNext = next == null ? null : periodRepository.save(next);
                    return new PotDelivery(savedCycle, delivered, savedNext);
                });
    }
}
