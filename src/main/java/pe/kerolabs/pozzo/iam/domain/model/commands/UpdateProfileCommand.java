package pe.kerolabs.pozzo.iam.domain.model.commands;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Theme;

import java.util.UUID;

/**
 * Command to change the profile of a member.
 *
 * @param accountId    the account of the member
 * @param displayName  the new display name
 * @param photoUrl     the new photo, or null to remove it
 * @param theme        the new visual theme
 * @param walletNumber the nine digits of the Yape or Plin number, or null to remove it
 * @param backupEmail  the backup email, or null to remove it
 */
public record UpdateProfileCommand(UUID accountId, String displayName, @Nullable String photoUrl, Theme theme,
                                   @Nullable String walletNumber, @Nullable String backupEmail) {
}
