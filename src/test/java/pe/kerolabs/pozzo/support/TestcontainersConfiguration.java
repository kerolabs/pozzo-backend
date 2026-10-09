package pe.kerolabs.pozzo.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Infrastructure shared by the integration and acceptance tests: a PostgreSQL 16 in a container,
 * the same engine the services use in production, an SMS sender that records the codes and a clock the
 * tests can move forward.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16-alpine");
    }

    @Bean
    @Primary
    RecordingSmsSender recordingSmsSender() {
        return new RecordingSmsSender();
    }

    @Bean
    @Primary
    MutableClock testClock() {
        return new MutableClock();
    }

    @Bean
    PozzoApi pozzoApi(MockMvc mockMvc, RecordingSmsSender sms) {
        return new PozzoApi(mockMvc, sms);
    }
}
