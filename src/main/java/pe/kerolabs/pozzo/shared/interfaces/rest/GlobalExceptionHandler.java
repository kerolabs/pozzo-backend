package pe.kerolabs.pozzo.shared.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.LocalizedMessages;

/**
 * Translates exceptions that escape the controllers into the standard error body,
 * so every failure of the REST API has the same shape.
 */
@Slf4j
@NullMarked
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean validation failures on a request body annotated with {@code @Valid}.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResource> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var prefix = LocalizedMessages.resolveOrDefault("validation.field.prefix", "Field");
        var fallback = LocalizedMessages.resolveOrDefault("validation.request.failed", "Request validation failed");
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> "%s %s: %s".formatted(prefix, error.getField(), error.getDefaultMessage()))
                .reduce((a, b) -> a + "; " + b)
                .orElse(fallback);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.validationError("request-body", details));
    }

    /**
     * Request bodies that cannot be parsed, such as malformed JSON or a wrong field type.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResource> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                "request-body",
                LocalizedMessages.resolveOrDefault("validation.request.failed", "Request validation failed")));
    }

    /**
     * Path or query parameters with the wrong type, such as an id that is not a UUID.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResource> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                ex.getName(),
                "Invalid value: %s".formatted(ex.getValue())));
    }

    /**
     * Invalid arguments rejected by the domain, such as a value object built with bad input.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResource> handleIllegalArgument(IllegalArgumentException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                LocalizedMessages.resolveOrDefault("validation.request.argument", "request-argument"),
                ex.getMessage() != null
                        ? ex.getMessage()
                        : LocalizedMessages.resolveOrDefault("validation.request.failed", "Request validation failed")));
    }

    /**
     * Requests to routes that do not exist.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResource> handleNoResourceFound(NoResourceFoundException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.notFound("Endpoint", ex.getResourcePath()));
    }

    /**
     * Last resort for any other exception. The cause is logged; the client gets a generic message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResource> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.unexpected(
                LocalizedMessages.resolveOrDefault("error.unexpected.context", "global-exception-handler"),
                ex.getClass().getSimpleName()));
    }
}
