package pe.kerolabs.pozzo.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Hands authentication and authorization failures over to the MVC exception resolver,
 * so they get the same error body as any other failure of the REST API.
 *
 * <p>These failures happen in the security filter chain, before the request reaches Spring MVC,
 * so the locale of the request has not been resolved yet. It is resolved here; otherwise the
 * message would follow the language of the server instead of the Accept-Language header.</p>
 */
@Component
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final LocaleResolver localeResolver;

    public UnauthorizedRequestHandlerEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
            LocaleResolver localeResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.localeResolver = localeResolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authenticationException) {
        resolveInRequestLocale(request, response, authenticationException);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        resolveInRequestLocale(request, response, accessDeniedException);
    }

    private void resolveInRequestLocale(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        var previous = LocaleContextHolder.getLocaleContext();
        LocaleContextHolder.setLocale(localeResolver.resolveLocale(request));
        try {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        } finally {
            LocaleContextHolder.setLocaleContext(previous);
        }
    }
}
