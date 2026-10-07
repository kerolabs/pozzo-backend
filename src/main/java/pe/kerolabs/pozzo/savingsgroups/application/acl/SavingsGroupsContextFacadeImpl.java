package pe.kerolabs.pozzo.savingsgroups.application.acl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.SavingsGroupRepository;
import pe.kerolabs.pozzo.savingsgroups.interfaces.acl.SavingsGroupsContextFacade;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Implements the Savings Groups facade on top of the savings group repository.
 */
@Service
@Transactional(readOnly = true)
public class SavingsGroupsContextFacadeImpl implements SavingsGroupsContextFacade {

    private final SavingsGroupRepository savingsGroupRepository;

    public SavingsGroupsContextFacadeImpl(SavingsGroupRepository savingsGroupRepository) {
        this.savingsGroupRepository = savingsGroupRepository;
    }

    @Override
    public boolean isOrganizer(UUID groupId, UUID accountId) {
        return savingsGroupRepository.findById(groupId).map(group -> group.isOrganizer(accountId)).orElse(false);
    }

    @Override
    public boolean isMember(UUID groupId, UUID accountId) {
        return savingsGroupRepository.findById(groupId).map(group -> group.isMember(accountId)).orElse(false);
    }

    @Override
    public boolean isOrganizerOfMember(UUID organizerAccountId, UUID memberAccountId) {
        return savingsGroupRepository.findAllByMemberId(memberAccountId).stream()
                .anyMatch(group -> group.isOrganizer(organizerAccountId));
    }

    @Override
    public List<GroupMember> fetchActiveMembers(UUID groupId) {
        return savingsGroupRepository.findById(groupId)
                .map(group -> group.activeMemberships().stream()
                        .sorted(Comparator.comparing((Membership membership) ->
                                        !membership.belongsTo(group.getOrganizerId()))
                                .thenComparing(Membership::getJoinedAt))
                        .map(membership -> new GroupMember(membership.getId(), membership.getMemberId(),
                                membership.getDisplayName(), membership.belongsTo(group.getOrganizerId())))
                        .toList())
                .orElse(List.of());
    }
}
