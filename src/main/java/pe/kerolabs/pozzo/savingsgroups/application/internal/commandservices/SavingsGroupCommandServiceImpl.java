package pe.kerolabs.pozzo.savingsgroups.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.savingsgroups.application.commandservices.SavingsGroupCommandService;
import pe.kerolabs.pozzo.savingsgroups.application.internal.outboundservices.acl.ExternalIamService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AddManualMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.CloseGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.CreateGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.DefineDestinationCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.DeleteGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.GenerateInvitationCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.JoinGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.RemoveMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.StartGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.UpdateRulesCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Destination;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupRules;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationCode;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.InvitationRepository;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.SavingsGroupRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Orchestrates the lifecycle of a savings group: create it, adjust its rules, invite and register
 * members, start it, delete it before it starts and close it when its cycle is over.
 */
@Service
@Transactional
public class SavingsGroupCommandServiceImpl implements SavingsGroupCommandService {

    /** Savings groups follow the calendar of Peru. */
    static final ZoneId GROUP_ZONE = ZoneId.of("America/Lima");
    private static final int CODE_GENERATION_ATTEMPTS = 5;

    private final SavingsGroupRepository savingsGroupRepository;
    private final InvitationRepository invitationRepository;
    private final ExternalIamService externalIamService;
    private final Clock clock;

    public SavingsGroupCommandServiceImpl(SavingsGroupRepository savingsGroupRepository,
                                          InvitationRepository invitationRepository,
                                          ExternalIamService externalIamService,
                                          Clock clock) {
        this.savingsGroupRepository = savingsGroupRepository;
        this.invitationRepository = invitationRepository;
        this.externalIamService = externalIamService;
        this.clock = clock;
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(CreateGroupCommand command) {
        var organizerName = externalIamService.fetchDisplayName(command.organizerId());
        if (organizerName.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Account", command.organizerId().toString()));
        }
        var dateError = checkFirstContributionDate(command.firstContributionDate());
        if (dateError != null) {
            return Result.failure(dateError);
        }
        var destination = command.destinationMethod() == null
                ? null
                : Destination.of(command.destinationMethod(), command.destinationPhoneNumber());
        var rules = new GroupRules(Money.soles(command.contributionAmount()), command.periodicity(),
                command.seats(), command.firstContributionDate(), destination);
        var group = SavingsGroup.create(command.organizerId(), organizerName.get(), command.name(), rules,
                clock.instant());
        return Result.success(savingsGroupRepository.save(group));
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(UpdateRulesCommand command) {
        var dateError = checkFirstContributionDate(command.firstContributionDate());
        if (dateError != null) {
            return Result.failure(dateError);
        }
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .map(group -> {
                    var rules = new GroupRules(Money.soles(command.contributionAmount()), command.periodicity(),
                            command.seats(), command.firstContributionDate(), group.getRules().destination());
                    group.updateRules(command.name(), rules);
                    return savingsGroupRepository.save(group);
                });
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(DefineDestinationCommand command) {
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .map(group -> {
                    group.defineDestination(Destination.of(command.method(), command.phoneNumber()));
                    return savingsGroupRepository.save(group);
                });
    }

    @Override
    public Result<Invitation, ApplicationError> handle(GenerateInvitationCommand command) {
        var now = clock.instant();
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .flatMap(group -> {
                    if (group.isStarted()) {
                        return Result.failure(ApplicationError.businessRuleViolation(
                                "SAVINGS_GROUP_ALREADY_STARTED", "A started group no longer accepts members"));
                    }
                    expireActiveInvitations(group.getId());
                    var invitation = Invitation.generate(group.getId(), command.requesterId(),
                            uniqueCodeFor(group.getName()), now);
                    return Result.success(invitationRepository.save(invitation));
                });
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(JoinGroupCommand command) {
        var now = clock.instant();
        var invitation = invitationRepository.findByCode(InvitationCode.parse(command.invitationCode()));
        if (invitation.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Invitation", command.invitationCode()));
        }
        if (!invitation.get().isUsable(now)) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "INVITATION_EXPIRED", "The invitation can no longer be used"));
        }
        var group = savingsGroupRepository.findById(invitation.get().getGroupId());
        if (group.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SavingsGroup", invitation.get().getGroupId().toString()));
        }
        var displayName = externalIamService.fetchDisplayName(command.memberId());
        if (displayName.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Account", command.memberId().toString()));
        }
        group.get().join(command.memberId(), displayName.get(), now);
        return Result.success(savingsGroupRepository.save(group.get()));
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(AddManualMemberCommand command) {
        var now = clock.instant();
        var phone = command.phoneNumber() == null || command.phoneNumber().isBlank()
                ? null
                : "+51" + command.phoneNumber().replace(" ", "");
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .map(group -> {
                    group.addManualMember(command.displayName(), phone, now);
                    return savingsGroupRepository.save(group);
                });
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(RemoveMemberCommand command) {
        var now = clock.instant();
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .flatMap(group -> {
                    if (group.findMembership(command.membershipId()).isEmpty()) {
                        return Result.failure(ApplicationError.notFound("Membership", command.membershipId().toString()));
                    }
                    group.removeMember(command.membershipId(), now);
                    return Result.success(savingsGroupRepository.save(group));
                });
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(StartGroupCommand command) {
        var now = clock.instant();
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .map(group -> {
                    group.start(now);
                    var started = savingsGroupRepository.save(group);
                    expireActiveInvitations(group.getId());
                    return started;
                });
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(DeleteGroupCommand command) {
        var now = clock.instant();
        return SavingsGroupAccess.requireOrganizer(savingsGroupRepository, command.groupId(), command.requesterId())
                .map(group -> {
                    group.delete(now);
                    invitationRepository.deleteAllByGroupId(group.getId());
                    savingsGroupRepository.delete(group);
                    return group;
                });
    }

    @Override
    public Result<SavingsGroup, ApplicationError> handle(CloseGroupCommand command) {
        var group = savingsGroupRepository.findById(command.groupId());
        if (group.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SavingsGroup", command.groupId().toString()));
        }
        group.get().close();
        return Result.success(savingsGroupRepository.save(group.get()));
    }

    private void expireActiveInvitations(UUID groupId) {
        invitationRepository.findAllActiveByGroupId(groupId).forEach(active -> {
            active.expire();
            invitationRepository.save(active);
        });
    }

    private InvitationCode uniqueCodeFor(String groupName) {
        for (int attempt = 0; attempt < CODE_GENERATION_ATTEMPTS; attempt++) {
            var code = InvitationCode.random(groupName);
            if (!invitationRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique invitation code");
    }

    private ApplicationError checkFirstContributionDate(LocalDate firstContributionDate) {
        var today = LocalDate.now(clock.withZone(GROUP_ZONE));
        if (firstContributionDate != null && firstContributionDate.isBefore(today)) {
            return ApplicationError.businessRuleViolation(
                    "FIRST_CONTRIBUTION_DATE_IN_PAST", "The first contribution date cannot be in the past");
        }
        return null;
    }
}
