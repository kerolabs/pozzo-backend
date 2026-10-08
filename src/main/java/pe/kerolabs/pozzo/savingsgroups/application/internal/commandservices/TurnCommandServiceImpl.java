package pe.kerolabs.pozzo.savingsgroups.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.savingsgroups.application.commandservices.TurnCommandService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AssignTurnsAgreedCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AssignTurnsByDrawCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.SavingsGroupRepository;
import pe.kerolabs.pozzo.savingsgroups.domain.services.TurnAssignmentService;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.HexFormat;

/**
 * Assigns the collection order of a savings group, by draw or as agreed by the group.
 */
@Service
@Transactional
public class TurnCommandServiceImpl implements TurnCommandService {

    private static final int SEED_BYTES = 8;

    private final SavingsGroupRepository savingsGroupRepository;
    private final TurnAssignmentService turnAssignmentService;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public TurnCommandServiceImpl(SavingsGroupRepository savingsGroupRepository,
                                  TurnAssignmentService turnAssignmentService,
                                  Clock clock) {
        this.savingsGroupRepository = savingsGroupRepository;
        this.turnAssignmentService = turnAssignmentService;
        this.clock = clock;
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(AssignTurnsByDrawCommand command) {
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .map(group -> {
                    var seed = newSeed();
                    group.assignTurns(turnAssignmentService.drawTurns(group, seed), TurnMethod.DRAW, seed,
                            clock.instant());
                    return savingsGroupRepository.save(group);
                });
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(AssignTurnsAgreedCommand command) {
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .map(group -> {
                    group.assignTurns(turnAssignmentService.agreedTurns(group, command.order()), TurnMethod.AGREED,
                            null, clock.instant());
                    return savingsGroupRepository.save(group);
                });
    }

    private String newSeed() {
        var bytes = new byte[SEED_BYTES];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
