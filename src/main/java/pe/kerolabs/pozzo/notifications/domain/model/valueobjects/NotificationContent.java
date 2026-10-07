package pe.kerolabs.pozzo.notifications.domain.model.valueobjects;

import org.jspecify.annotations.Nullable;

/**
 * What a notification says and where it leads in the application.
 *
 * @param title    the title, up to 80 characters
 * @param body     the text, up to 240 characters
 * @param deepLink the screen it opens, e.g. "pozzo://groups/{id}"
 */
public record NotificationContent(String title, String body, @Nullable String deepLink) {

    public NotificationContent {
        if (title == null || title.isBlank() || title.length() > 80) {
            throw new IllegalArgumentException("The title is required and cannot exceed 80 characters");
        }
        if (body == null || body.isBlank() || body.length() > 240) {
            throw new IllegalArgumentException("The body is required and cannot exceed 240 characters");
        }
        if (deepLink != null && deepLink.length() > 200) {
            throw new IllegalArgumentException("The deep link cannot exceed 200 characters");
        }
    }
}
