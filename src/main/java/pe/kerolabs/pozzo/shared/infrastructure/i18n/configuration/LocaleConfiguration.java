package pe.kerolabs.pozzo.shared.infrastructure.i18n.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

/**
 * Resolves the locale of each REST request from the Accept-Language header.
 * Spanish is the default because the application targets users in Peru.
 */
@Configuration
public class LocaleConfiguration {

    private static final Locale SPANISH = Locale.forLanguageTag("es");

    /**
     * Configures the default HTTP request {@link LocaleResolver} based on the Accept-Language header.
     * Sets Spanish (es) as default locale with English (en) as fallback option.
     *
     * @return the configured {@link AcceptHeaderLocaleResolver} instance
     */
    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(SPANISH);
        resolver.setSupportedLocales(List.of(SPANISH, Locale.ENGLISH));
        return resolver;
    }
}
