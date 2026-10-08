package pe.kerolabs.pozzo.iam.domain.model.commands;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Command to set or remove the profile photo of a member.
 *
 * @param accountId   the account of the member
 * @param content     the image bytes, or null to remove the photo
 * @param contentType the media type of the image, or null to remove the photo
 */
public record ChangeProfilePhotoCommand(UUID accountId, byte @Nullable [] content, @Nullable String contentType) {

    public static ChangeProfilePhotoCommand remove(UUID accountId) {
        return new ChangeProfilePhotoCommand(accountId, null, null);
    }
}
