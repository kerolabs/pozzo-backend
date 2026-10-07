package pe.kerolabs.pozzo.shared.application.result;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Represents an error that occurred in the application layer.
 * Designed to be mapped to HTTP responses and to carry structured error information.
 *
 * @param type    the error category, which determines the HTTP status
 * @param code    a machine-readable error code (e.g. "SAVINGS_GROUP_NOT_FOUND", "INVALID_VERIFICATION_CODE")
 * @param message a human-readable error message, used when no localized message exists for the code
 * @param details optional additional context about the error
 */
@NullMarked
public record ApplicationError(
        ErrorType type,
        String code,
        String message,
        @Nullable String details) {

    /**
     * Validation error: input data is invalid or violates constraints.
     */
    public static ApplicationError validationError(String fieldOrConcept, String reason) {
        return new ApplicationError(
                ErrorType.VALIDATION,
                "VALIDATION_ERROR",
                "Validation failed: %s".formatted(fieldOrConcept),
                reason);
    }

    /**
     * Unauthorized error: the caller is not authenticated or the credentials are invalid.
     *
     * @param code   a specific code, so the client can tell the cases apart
     * @param reason a human-readable explanation
     */
    public static ApplicationError unauthorized(String code, String reason) {
        return new ApplicationError(ErrorType.UNAUTHORIZED, code, "Authentication failed", reason);
    }

    /**
     * Forbidden error: the caller is authenticated but not allowed to perform the operation.
     *
     * @param code   a specific code, so the client can tell the cases apart
     * @param reason a human-readable explanation
     */
    public static ApplicationError forbidden(String code, String reason) {
        return new ApplicationError(ErrorType.FORBIDDEN, code, "Operation not allowed", reason);
    }

    /**
     * Not found error: the requested resource does not exist.
     */
    public static ApplicationError notFound(String resourceType, String identifier) {
        return new ApplicationError(
                ErrorType.NOT_FOUND,
                "%s_NOT_FOUND".formatted(toCodeSegment(resourceType)),
                "%s not found: %s".formatted(resourceType, identifier),
                null);
    }

    /**
     * Conflict error: the operation cannot be completed because of the current state.
     */
    public static ApplicationError conflict(String resource, String reason) {
        return new ApplicationError(
                ErrorType.CONFLICT,
                "%s_CONFLICT".formatted(toCodeSegment(resource)),
                "Conflict with %s".formatted(resource),
                reason);
    }

    /**
     * Business rule violation error: the operation violates a domain rule.
     *
     * @param code   a specific code for the rule (e.g. "TERMS_NOT_ACCEPTED")
     * @param reason a human-readable explanation
     */
    public static ApplicationError businessRuleViolation(String code, String reason) {
        return new ApplicationError(ErrorType.BUSINESS_RULE, code, "Business rule violation", reason);
    }

    /**
     * Too many requests error: the operation was attempted again before it was allowed.
     *
     * @param code   a specific code (e.g. "VERIFICATION_CODE_RESEND_TOO_SOON")
     * @param reason a human-readable explanation
     */
    public static ApplicationError tooManyRequests(String code, String reason) {
        return new ApplicationError(ErrorType.TOO_MANY_REQUESTS, code, "Too many requests", reason);
    }

    /**
     * Unexpected error: something went wrong that should not have.
     */
    public static ApplicationError unexpected(String context, String reason) {
        return new ApplicationError(
                ErrorType.UNEXPECTED,
                "UNEXPECTED_ERROR",
                "Unexpected error in %s".formatted(context),
                reason);
    }

    private static String toCodeSegment(String name) {
        return name.trim().replaceAll("([a-z])([A-Z])", "$1_$2").replaceAll("[\\s-]+", "_").toUpperCase();
    }
}
