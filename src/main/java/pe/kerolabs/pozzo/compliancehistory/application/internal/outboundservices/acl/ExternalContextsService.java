package pe.kerolabs.pozzo.compliancehistory.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.iam.interfaces.acl.IamContextFacade;
import pe.kerolabs.pozzo.savingsgroups.interfaces.acl.SavingsGroupsContextFacade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-corruption layer towards Identity and Access and Savings Groups: the name a member shows,
 * who organizes which group and who belongs to it, translated to what Compliance History needs.
 */
@Service
public class ExternalContextsService {

    private final IamContextFacade iamContextFacade;
    private final SavingsGroupsContextFacade savingsGroupsContextFacade;

    public ExternalContextsService(IamContextFacade iamContextFacade,
                                   SavingsGroupsContextFacade savingsGroupsContextFacade) {
        this.iamContextFacade = iamContextFacade;
        this.savingsGroupsContextFacade = savingsGroupsContextFacade;
    }

    public Optional<String> fetchDisplayName(UUID accountId) {
        return iamContextFacade.fetchDisplayNameByAccountId(accountId);
    }

    public boolean isOrganizer(UUID groupId, UUID accountId) {
        return savingsGroupsContextFacade.isOrganizer(groupId, accountId);
    }

    public boolean isOrganizerOfMember(UUID organizerAccountId, UUID memberAccountId) {
        return savingsGroupsContextFacade.isOrganizerOfMember(organizerAccountId, memberAccountId);
    }

    /**
     * The active members of a group: membership, account (null without the application), name and role.
     */
    public List<GroupMember> fetchActiveMembers(UUID groupId) {
        return savingsGroupsContextFacade.fetchActiveMembers(groupId).stream()
                .map(member -> new GroupMember(member.membershipId(), member.accountId(), member.displayName(),
                        member.organizer()))
                .toList();
    }

    public record GroupMember(UUID membershipId, UUID accountId, String displayName, boolean organizer) {
    }
}
