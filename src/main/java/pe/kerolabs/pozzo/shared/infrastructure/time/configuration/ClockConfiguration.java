package pe.kerolabs.pozzo.shared.infrastructure.time.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides the clock that application services use to read the current time,
 * so time-dependent rules (expirations, waiting periods) can be tested with a fixed clock.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
