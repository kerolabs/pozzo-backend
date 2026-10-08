package pe.kerolabs.pozzo.iam.domain.model.valueobjects;

import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * The member's own data: the display name and photo the group sees, the visual theme, and the contact
 * data only the member sees, the Yape or Plin number and a backup email.
 *
 * @param displayName  the name shown to the group
 * @param photoUrl     an optional photo
 * @param theme        the visual theme of the application
 * @param walletNumber the Yape or Plin number where the member receives transfers, if given; it is
 *                     offered as the destination of the groups the member creates
 * @param backupEmail  an optional email to reach the member if the phone is lost
 */
public record Profile(String displayName, @Nullable String photoUrl, Theme theme,
                      @Nullable PhoneNumber walletNumber, @Nullable String backupEmail) {

    public static final int DISPLAY_NAME_MAX_LENGTH = 80;
    public static final int PHOTO_URL_MAX_LENGTH = 300;
    public static final int BACKUP_EMAIL_MAX_LENGTH = 120;
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

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
        backupEmail = backupEmail == null || backupEmail.isBlank() ? null : backupEmail.strip().toLowerCase();
        if (backupEmail != null && (backupEmail.length() > BACKUP_EMAIL_MAX_LENGTH || !EMAIL.matcher(backupEmail).matches())) {
            throw new IllegalArgumentException("The backup email is not valid");
        }
    }

    /**
     * Creates the profile of a new member, who follows the system theme until choosing another and has
     * not given contact data yet.
     */
    public static Profile of(String displayName, @Nullable String photoUrl) {
        return new Profile(displayName, photoUrl, Theme.SYSTEM, null, null);
    }
}
