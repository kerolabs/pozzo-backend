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

    /**
     * Exposes a system UTC {@link Clock} bean for consistent application time queries.
     * Can be replaced by fixed clocks in unit and integration test configurations.
     *
     * @return the application's root {@link Clock} bean
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
