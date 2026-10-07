package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform;

import pe.kerolabs.pozzo.savingsgroups.application.queryservices.GroupPreview;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.GroupPreviewResource;

/**
 * Converts a {@link GroupPreview} into {@link GroupPreviewResource}, without the member list
 * or the destination number.
 */
public class GroupPreviewResourceFromEntityAssembler {

    public static GroupPreviewResource toResourceFromEntity(GroupPreview preview) {
        var group = preview.group();
        return new GroupPreviewResource(
                group.getId(),
                group.getName(),
                group.getStatus(),
                GroupResourceFromEntityAssembler.organizerName(group),
                RulesResourceFromEntityAssembler.toResourceFromEntity(group.getRules(), false),
                group.activeMembersCount(),
                group.freeSeats(),
                preview.invitation().getExpiresAt());
    }
}
