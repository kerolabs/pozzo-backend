package pe.kerolabs.pozzo.shared.interfaces.rest.transform;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Resolves messages from the {@code messages} bundle in the locale of the current request.
 *
 * <p>Fallback to the JVM locale is disabled: a request in Spanish on a server whose default
 * locale is English must get the Spanish base bundle, not the English one.</p>
 */
@NullMarked
public final class LocalizedMessages {

    private static final String MESSAGES_BASENAME = "messages";
    private static final ResourceBundle.Control NO_FALLBACK =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private LocalizedMessages() {
    }

    /**
     * Returns the message for the key, or {@code null} when the bundle does not define it.
     */
    public static @Nullable String resolveOrNull(String key, Object... args) {
        try {
            var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, LocaleContextHolder.getLocale(), NO_FALLBACK);
            if (!bundle.containsKey(key)) {
                return null;
            }
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    /**
     * Returns the message for the key, or the given default when the bundle does not define it.
     */
    public static String resolveOrDefault(String key, String defaultValue, Object... args) {
        var message = resolveOrNull(key, args);
        return message != null ? message : defaultValue;
    }
}
