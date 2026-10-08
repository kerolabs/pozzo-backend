package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Request to set the collection order the group agreed on.
 */
@Schema(name = "AgreedTurnsRequest", description = "Collection order agreed by the group")
public record AgreedTurnsResource(
        @ArraySchema(arraySchema = @Schema(description = "Memberships in collection order: the first collects first"))
        @NotEmpty
        List<UUID> order) {
}
