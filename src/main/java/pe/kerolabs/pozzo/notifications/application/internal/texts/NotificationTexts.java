package pe.kerolabs.pozzo.notifications.application.internal.texts;

import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationContent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * The texts members read on their phones. They are in Spanish because Pozzo serves groups in Peru;
 * they are cut to the limits of a notification so a long group name never breaks a delivery.
 */
public final class NotificationTexts {

    private static final Locale SPANISH = Locale.of("es", "PE");
    private static final DateTimeFormatter CUTOFF_FORMAT = DateTimeFormatter.ofPattern("d 'de' MMMM", SPANISH);
    private static final int TITLE_MAX = 80;
    private static final int BODY_MAX = 240;

    private NotificationTexts() {
    }

    public static NotificationContent reminder(int daysBefore, String groupName, BigDecimal amount,
                                               LocalDate cutoffDate, UUID groupId) {
        var title = switch (daysBefore) {
            case 0 -> "Hoy es el día de corte";
            case 1 -> "Mañana es el día de corte";
            default -> "Faltan %d días para el corte".formatted(daysBefore);
        };
        return content(title, "Aporta %s a %s. Fecha límite: %s."
                .formatted(money(amount), groupName, CUTOFF_FORMAT.format(cutoffDate)), groupLink(groupId));
    }

    public static NotificationContent groupStarted(String groupName, int turnNumber, UUID groupId) {
        return content("La junta inició", "%s ya tiene sus turnos. Te toca cobrar en el turno %d."
                .formatted(groupName, turnNumber), groupLink(groupId));
    }

    public static NotificationContent contributionSettled(String groupName, BigDecimal amount, boolean late,
                                                          UUID groupId) {
        var body = late
                ? "Tu aporte de %s a %s quedó registrado fuera de fecha."
                : "Tu aporte de %s a %s quedó validado.";
        return content("Aporte registrado", body.formatted(money(amount), groupName), groupLink(groupId));
    }

    public static NotificationContent contributionCovered(String groupName, BigDecimal amount, UUID groupId) {
        return content("Aporte cubierto", "El organizador cubrió tu aporte de %s en %s."
                .formatted(money(amount), groupName), groupLink(groupId));
    }

    public static NotificationContent reviewRequired(String memberName, String groupName, UUID groupId) {
        return content("Comprobante por revisar", "%s subió un comprobante con diferencias en %s."
                .formatted(memberName, groupName), "pozzo://groups/%s/reviews".formatted(groupId));
    }

    public static NotificationContent contributionRejected(String groupName, UUID groupId) {
        return content("Comprobante rechazado", "El organizador rechazó tu comprobante en %s. Vuelve a registrar tu aporte."
                .formatted(groupName), groupLink(groupId));
    }

    public static NotificationContent potCompleted(String groupName, int turnNumber, BigDecimal potAmount,
                                                   String payoutName, UUID groupId) {
        return content("Pozo completo", "%s juntó %s del turno %d. Entrégaselo a %s."
                .formatted(groupName, money(potAmount), turnNumber, payoutName), groupLink(groupId));
    }

    public static NotificationContent potDelivered(String groupName, BigDecimal potAmount, UUID groupId) {
        return content("Recibiste el pozo", "El organizador registró la entrega de %s de %s."
                .formatted(money(potAmount), groupName), groupLink(groupId));
    }

    public static NotificationContent memberJoined(String memberName, String groupName, int membersCount, int seats,
                                                   UUID groupId) {
        var body = membersCount >= seats
                ? "%s se unió a %s. La junta está completa: ya puedes asignar los turnos.".formatted(memberName, groupName)
                : "%s se unió a %s. Van %d de %d integrantes.".formatted(memberName, groupName, membersCount, seats);
        return content("Nuevo integrante", body, "pozzo://groups/%s/detail".formatted(groupId));
    }

    public static NotificationContent rulesUpdated(String groupName, BigDecimal amount, String periodicity, int seats,
                                                   UUID groupId) {
        var every = switch (periodicity) {
            case "WEEKLY" -> "semanal";
            case "BIWEEKLY" -> "quincenal";
            default -> "mensual";
        };
        return content("Cambiaron las reglas", "La cabeza actualizó %s: aporte %s %s, %d integrantes."
                .formatted(groupName, every, money(amount), seats), "pozzo://groups/%s/detail".formatted(groupId));
    }

    public static NotificationContent groupFilled(String groupName, UUID groupId) {
        return content("Junta completa", "%s ya tiene a todos sus integrantes. Falta que la cabeza asigne los turnos."
                .formatted(groupName), "pozzo://groups/%s/detail".formatted(groupId));
    }

    public static NotificationContent groupDeleted(String groupName) {
        return content("Junta eliminada", "La cabeza eliminó %s antes de iniciarla.".formatted(groupName),
                "pozzo://groups");
    }

    public static NotificationContent cycleClosed(String groupName) {
        return content("Ciclo terminado", "%s completó todos sus turnos. Tu historial de cumplimiento ya está actualizado."
                .formatted(groupName), "pozzo://compliance");
    }

    /**
     * Soles without decimals when the amount is whole: "S/ 300", "S/ 300.50".
     */
    static String money(BigDecimal amount) {
        var scaled = amount.setScale(2, RoundingMode.HALF_UP);
        var whole = scaled.stripTrailingZeros().scale() <= 0;
        return "S/ " + (whole ? scaled.setScale(0, RoundingMode.UNNECESSARY).toPlainString() : scaled.toPlainString());
    }

    private static String groupLink(UUID groupId) {
        return "pozzo://groups/%s".formatted(groupId);
    }

    private static NotificationContent content(String title, String body, String deepLink) {
        return new NotificationContent(fit(title, TITLE_MAX), fit(body, BODY_MAX), deepLink);
    }

    private static String fit(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }
}
