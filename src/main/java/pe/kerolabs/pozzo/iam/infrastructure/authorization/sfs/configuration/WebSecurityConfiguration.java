package pe.kerolabs.pozzo.iam.infrastructure.authorization.sfs.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import pe.kerolabs.pozzo.iam.application.queryservices.AccountQueryService;
import pe.kerolabs.pozzo.iam.infrastructure.authorization.sfs.pipeline.BearerAuthorizationRequestFilter;
import pe.kerolabs.pozzo.iam.infrastructure.authorization.sfs.pipeline.UnauthorizedRequestHandlerEntryPoint;

import java.util.List;

/**
 * Security configuration of the REST API: stateless, authenticated with the bearer token,
 * with the access endpoints and the documentation open to everyone.
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfiguration {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health",
            "/error"
    };

    private static final String[] PUBLIC_AUTHENTICATION = {
            "/api/v1/auth/codes",
            "/api/v1/auth/codes/verify",
            "/api/v1/auth/register"
    };

    private final AccountQueryService accountQueryService;
    private final UnauthorizedRequestHandlerEntryPoint unauthorizedRequestHandler;

    public WebSecurityConfiguration(AccountQueryService accountQueryService,
                                    UnauthorizedRequestHandlerEntryPoint unauthorizedRequestHandler) {
        this.accountQueryService = accountQueryService;
        this.unauthorizedRequestHandler = unauthorizedRequestHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(request -> {
                    var configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of("*"));
                    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
                    configuration.setAllowedHeaders(List.of("*"));
                    return configuration;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(unauthorizedRequestHandler)
                        .accessDeniedHandler(unauthorizedRequestHandler))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_AUTHENTICATION).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new BearerAuthorizationRequestFilter(accountQueryService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
