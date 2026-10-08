package pe.kerolabs.pozzo.savingsgroups.application.internal.commandservices;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.SavingsGroupRepository;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.util.UUID;

/**
 * Who may act on a savings group.
 *
 * <p>A requester who is not a member gets "not found", the same answer as for a group that does not
 * exist, so the API does not reveal which groups exist. A member who is not the organizer gets
 * "forbidden" for the operations reserved to the organizer.</p>
 */
final class SavingsGroupAccess {

    private SavingsGroupAccess() {
    }

    /**
     * Loads a group the requester organizes.
     */
    static Result<SavingsGroup, ApplicationError> requireOrganizer(SavingsGroupRepository repository,
                                                                  UUID groupId, UUID requesterId) {
        var group = repository.findById(groupId).filter(found -> found.isMember(requesterId));
        if (group.isEmpty()) {
            return Result.failure(ApplicationError.notFound("SavingsGroup", groupId.toString()));
        }
        if (!group.get().isOrganizer(requesterId)) {
            return Result.failure(ApplicationError.forbidden(
                    "ONLY_ORGANIZER", "Only the organizer of the group can do this"));
        }
        return Result.success(group.get());
    }
}
