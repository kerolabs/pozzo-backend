package pe.kerolabs.pozzo.notifications.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.savingsgroups.interfaces.acl.SavingsGroupsContextFacade;

import java.util.UUID;

/**
 * Anti-corruption layer towards Savings Groups: who belongs to a group and who organizes it.
 */
@Service
public class ExternalSavingsGroupsService {

    private final SavingsGroupsContextFacade savingsGroupsContextFacade;

    public ExternalSavingsGroupsService(SavingsGroupsContextFacade savingsGroupsContextFacade) {
        this.savingsGroupsContextFacade = savingsGroupsContextFacade;
    }

    /**
     * Verifies whether an account is an enrolled member in the specified savings group.
     *
     * @param groupId the savings group identifier
     * @param accountId the member account identifier
     * @return true if the account is an active member
     */
    public boolean isMember(UUID groupId, UUID accountId) {
        return savingsGroupsContextFacade.isMember(groupId, accountId);
    }

    /**
     * Verifies whether an account is the organizer of the specified savings group.
     *
     * @param groupId the savings group identifier
     * @param accountId the member account identifier
     * @return true if the account is the group organizer
     */
    public boolean isOrganizer(UUID groupId, UUID accountId) {
        return savingsGroupsContextFacade.isOrganizer(groupId, accountId);
    }
}
