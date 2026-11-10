package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Who takes the seat of a member who dropped out.
 */
@Schema(name = "ReplaceMember", description = "Who takes the seat of a member who dropped out")
public record ReplaceMemberResource(
        @Schema(description = "Name of the replacement; ignored when the number belongs to a Pozzo account",
                example = "Rosa Medina")
        @NotBlank
        @Size(max = 80)
        String displayName,

        @Schema(description = "Optional nine-digit number. When someone signed up in Pozzo with it, they take the "
                + "seat with their account", example = "999000000", nullable = true)
        @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
        String phoneNumber) {
}
