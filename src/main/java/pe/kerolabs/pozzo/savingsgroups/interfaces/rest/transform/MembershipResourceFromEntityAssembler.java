package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnSlot;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.MembershipResource;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts the active members of a {@link SavingsGroup} into {@link MembershipResource} items,
 * with the organizer first and the rest in the order they joined.
 */
public class MembershipResourceFromEntityAssembler {

    public static List<MembershipResource> toResourcesFromEntity(SavingsGroup group, UUID requesterId,
                                                              Map<UUID, String> photosByAccount) {
        var requesterIsOrganizer = group.isOrganizer(requesterId);
        return group.activeMemberships().stream()
                .sorted(Comparator.comparing((Membership membership) -> !membership.belongsTo(group.getOrganizerId()))
                        .thenComparing(Membership::getJoinedAt))
                .map(membership -> new MembershipResource(
                        membership.getId(),
                        membership.getDisplayName(),
                        membership.getKind(),
                        membership.getStatus(),
                        membership.belongsTo(group.getOrganizerId()),
                        membership.belongsTo(requesterId),
                        requesterIsOrganizer && membership.getPhone() != null
                                ? membership.getPhone().replace("+51", "")
                                : null,
                        group.turnOf(membership.getId()).map(TurnSlot::turnNumber).orElse(null),
                        membership.getJoinedAt(),
                        membership.getMemberId() == null ? null : photosByAccount.get(membership.getMemberId()),
                        membership.getMemberId()))
                .toList();
    }
}
