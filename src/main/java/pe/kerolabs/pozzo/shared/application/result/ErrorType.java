package pe.kerolabs.pozzo.shared.application.result;

/**
 * Category of an {@link ApplicationError}. The REST layer derives the HTTP status from it,
 * so an error code can be as specific as the use case needs without a new mapping rule.
 */
public enum ErrorType {
    VALIDATION,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    BUSINESS_RULE,
    TOO_MANY_REQUESTS,
    UNEXPECTED
}
