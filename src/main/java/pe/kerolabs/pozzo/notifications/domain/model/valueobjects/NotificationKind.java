package pe.kerolabs.pozzo.notifications.domain.model.valueobjects;

/**
 * A REMINDER is scheduled before a cutoff date and cancelled when the member pays;
 * an ALERT tells the member that something happened and goes out right away.
 */
public enum NotificationKind {
    REMINDER,
    ALERT
}
