package pe.kerolabs.pozzo.acceptance;

import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.spring.ScenarioScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import pe.kerolabs.pozzo.support.PozzoIntegrationTest;

/**
 * The acceptance tests run against the whole application and the PostgreSQL container, like the
 * integration tests. Each scenario gets its own {@link ScenarioState}.
 */
@CucumberContextConfiguration
@PozzoIntegrationTest
@Import(CucumberSpringConfiguration.ScenarioBeans.class)
public class CucumberSpringConfiguration {

    @TestConfiguration(proxyBeanMethods = false)
    static class ScenarioBeans {

        @Bean
        @ScenarioScope
        ScenarioState scenarioState() {
            return new ScenarioState();
        }
    }
}
