package pe.kerolabs.pozzo.savingsgroups.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.savingsgroups.application.queryservices.GroupPreview;
import pe.kerolabs.pozzo.savingsgroups.application.queryservices.SavingsGroupQueryService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetActiveInvitationQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetGroupByIdQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetGroupPreviewQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetMembersQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetMyGroupsQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetTurnCalendarQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationCode;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.InvitationRepository;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.SavingsGroupRepository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves savings group read queries, only for the members of each group.
 */
@Service
@Transactional(readOnly = true)
public class SavingsGroupQueryServiceImpl implements SavingsGroupQueryService {

    private final SavingsGroupRepository savingsGroupRepository;
    private final InvitationRepository invitationRepository;
    private final Clock clock;

    public SavingsGroupQueryServiceImpl(SavingsGroupRepository savingsGroupRepository,
                                        InvitationRepository invitationRepository,
                                        Clock clock) {
        this.savingsGroupRepository = savingsGroupRepository;
        this.invitationRepository = invitationRepository;
        this.clock = clock;
    }

    @Override
    public Optional<SavingsGroup> handle(GetGroupByIdQuery query) {
        return findForMember(query.groupId(), query.requesterId());
    }

    @Override
    public Optional<SavingsGroup> handle(GetMembersQuery query) {
        return findForMember(query.groupId(), query.requesterId());
    }

    @Override
    public Optional<SavingsGroup> handle(GetTurnCalendarQuery query) {
        return findForMember(query.groupId(), query.requesterId());
    }

    @Override
    public Optional<GroupPreview> handle(GetGroupPreviewQuery query) {
        var now = clock.instant();
        return invitationRepository.findByCode(InvitationCode.parse(query.invitationCode()))
                .filter(invitation -> invitation.isUsable(now))
                .flatMap(invitation -> savingsGroupRepository.findById(invitation.getGroupId())
                        .map(group -> new GroupPreview(group, invitation)));
    }

    @Override
    public List<SavingsGroup> handle(GetMyGroupsQuery query) {
        return savingsGroupRepository.findAllByMemberId(query.memberId());
    }

    @Override
    public Optional<Invitation> handle(GetActiveInvitationQuery query) {
        var now = clock.instant();
        return findForMember(query.groupId(), query.requesterId())
                .filter(group -> group.isOrganizer(query.requesterId()))
                .flatMap(group -> invitationRepository.findAllActiveByGroupId(group.getId()).stream()
                        .filter(invitation -> invitation.isUsable(now))
                        .findFirst());
    }

    private Optional<SavingsGroup> findForMember(UUID groupId, UUID requesterId) {
        return savingsGroupRepository.findById(groupId).filter(group -> group.isMember(requesterId));
    }
}
