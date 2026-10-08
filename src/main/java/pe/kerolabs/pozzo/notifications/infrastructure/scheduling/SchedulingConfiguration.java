package pe.kerolabs.pozzo.notifications.infrastructure.scheduling;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's background scheduled task execution throughout the application.
 * Enables recurring background tasks, including the periodic dispatch of due push notifications
 * and reminders to participants.
 */
@Configuration
@EnableScheduling
public class SchedulingConfiguration {
}
