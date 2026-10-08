package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform;

import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.AddManualMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.CreateGroupCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.DefineDestinationCommand;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.UpdateRulesCommand;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.AddManualMemberResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.CreateGroupResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.DestinationResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.UpdateRulesResource;

import java.util.UUID;

/**
 * Translates the request resources of Savings Groups into commands, adding the authenticated
 * member as organizer or requester. The requester never comes from the request body.
 */
public class GroupCommandFromResourceAssembler {

    public static CreateGroupCommand toCommandFromResource(UUID organizerId, CreateGroupResource resource) {
        var destination = resource.destination();
        return new CreateGroupCommand(
                organizerId,
                resource.name(),
                resource.contributionAmount(),
                resource.periodicity(),
                resource.seats(),
                resource.firstContributionDate(),
                destination == null ? null : destination.method(),
                destination == null ? null : destination.phoneNumber());
    }

    public static UpdateRulesCommand toCommandFromResource(UUID groupId, UUID requesterId,
                                                           UpdateRulesResource resource) {
        return new UpdateRulesCommand(groupId, requesterId, resource.name(), resource.contributionAmount(),
                resource.periodicity(), resource.seats(), resource.firstContributionDate());
    }

    public static DefineDestinationCommand toCommandFromResource(UUID groupId, UUID requesterId,
                                                                 DestinationResource resource) {
        return new DefineDestinationCommand(groupId, requesterId, resource.method(), resource.phoneNumber());
    }

    public static AddManualMemberCommand toCommandFromResource(UUID groupId, UUID requesterId,
                                                               AddManualMemberResource resource) {
        return new AddManualMemberCommand(groupId, requesterId, resource.displayName(), resource.phoneNumber());
    }
}
