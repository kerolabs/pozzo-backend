package pe.kerolabs.pozzo.iam.domain.model.valueobjects;

import org.jspecify.annotations.Nullable;

/**
 * What the other members of a savings group see of a member: display name, photo and visual theme.
 *
 * @param displayName the name shown to the group
 * @param photoUrl    an optional photo
 * @param theme       the visual theme of the application
 */
public record Profile(String displayName, @Nullable String photoUrl, Theme theme) {

    public static final int DISPLAY_NAME_MAX_LENGTH = 80;
    public static final int PHOTO_URL_MAX_LENGTH = 300;

    public Profile {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("The display name is required");
        }
        displayName = displayName.strip();
        if (displayName.length() > DISPLAY_NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("The display name cannot exceed %d characters".formatted(DISPLAY_NAME_MAX_LENGTH));
        }
        if (photoUrl != null && photoUrl.length() > PHOTO_URL_MAX_LENGTH) {
            throw new IllegalArgumentException("The photo URL cannot exceed %d characters".formatted(PHOTO_URL_MAX_LENGTH));
        }
        if (theme == null) {
            throw new IllegalArgumentException("The theme is required");
        }
    }

    /**
     * Creates the profile of a new member, who follows the system theme until choosing another.
     */
    public static Profile of(String displayName, @Nullable String photoUrl) {
        return new Profile(displayName, photoUrl, Theme.SYSTEM);
    }
}
