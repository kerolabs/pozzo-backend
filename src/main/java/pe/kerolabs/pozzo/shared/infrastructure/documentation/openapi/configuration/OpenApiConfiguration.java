package pe.kerolabs.pozzo.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI specification exposed by the platform.
 *
 * <p>No server list is declared: Swagger UI uses the host it was loaded from, so the same
 * document works locally and in production.</p>
 */
@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${documentation.application.title}")
    String applicationTitle;

    @Value("${documentation.application.description}")
    String applicationDescription;

    @Value("${documentation.application.version}")
    String applicationVersion;

    /**
     * Builds the OpenAPI document used by Swagger UI and client generation tools.
     *
     * @return the configured OpenAPI descriptor
     */
    @Bean
    public OpenAPI pozzoPlatformOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(this.applicationTitle)
                        .description(this.applicationDescription)
                        .version(this.applicationVersion)
                        .contact(new Contact()
                                .name("Kerolabs")
                                .url("https://kerolabs.github.io/pozzo-landing-page/")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT bearer token issued by the Identity and Access context")));
    }
}
