package pe.kerolabs.pozzo.shared.application.result;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Represents an error that occurred in the application layer.
 * Designed to be mapped to HTTP responses and to carry structured error information.
 *
 * @param code    a machine-readable error code (e.g. "SAVINGS_GROUP_NOT_FOUND", "VALIDATION_ERROR")
 * @param message a human-readable error message
 * @param details optional additional context about the error
 */
@NullMarked
public record ApplicationError(
        String code,
        String message,
        @Nullable String details) {

    /**
     * Creates an ApplicationError with code and message only.
     */
    public ApplicationError(String code, String message) {
        this(code, message, null);
    }

    /**
     * Validation error: input data is invalid or violates constraints.
     */
    public static ApplicationError validationError(String fieldOrConcept, String reason) {
        return new ApplicationError(
                "VALIDATION_ERROR",
                "Validation failed: %s".formatted(fieldOrConcept),
                reason);
    }

    /**
     * Not found error: the requested resource does not exist.
     */
    public static ApplicationError notFound(String resourceType, String identifier) {
        return new ApplicationError(
                "%s_NOT_FOUND".formatted(toCodeSegment(resourceType)),
                "%s not found: %s".formatted(resourceType, identifier),
                null);
    }

    /**
     * Business rule violation error: the operation violates a domain rule.
     */
    public static ApplicationError businessRuleViolation(String rule, String reason) {
        return new ApplicationError(
                "BUSINESS_RULE_VIOLATION",
                "Business rule violation: %s".formatted(rule),
                reason);
    }

    /**
     * Conflict error: the operation cannot be completed because of the current state.
     */
    public static ApplicationError conflict(String resource, String reason) {
        return new ApplicationError(
                "%s_CONFLICT".formatted(toCodeSegment(resource)),
                "Conflict with %s".formatted(resource),
                reason);
    }

    /**
     * Unauthorized error: the caller is not authenticated or the credentials are invalid.
     */
    public static ApplicationError unauthorized(String reason) {
        return new ApplicationError(
                "UNAUTHORIZED",
                "Authentication required",
                reason);
    }

    /**
     * Forbidden error: the caller is authenticated but not allowed to perform the operation.
     */
    public static ApplicationError forbidden(String reason) {
        return new ApplicationError(
                "FORBIDDEN",
                "Operation not allowed",
                reason);
    }

    /**
     * Unexpected error: something went wrong that should not have.
     */
    public static ApplicationError unexpected(String context, String reason) {
        return new ApplicationError(
                "UNEXPECTED_ERROR",
                "Unexpected error in %s".formatted(context),
                reason);
    }

    private static String toCodeSegment(String name) {
        return name.trim().replaceAll("([a-z])([A-Z])", "$1_$2").replaceAll("[\\s-]+", "_").toUpperCase();
    }
}
