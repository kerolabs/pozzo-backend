package pe.kerolabs.pozzo.shared.domain.exceptions;

import lombok.Getter;

/**
 * Thrown by an aggregate when an operation would break one of its business rules,
 * e.g. joining a savings group that has no free seats.
 *
 * <p>The REST layer maps it to {@code 422 Unprocessable Content} with the code, so the
 * application layer does not have to repeat the checks the aggregate already makes.</p>
 */
@Getter
public class BusinessRuleViolationException extends RuntimeException {

    /** Machine-readable code of the rule, e.g. "SAVINGS_GROUP_FULL". */
    private final String code;

    public BusinessRuleViolationException(String code, String message) {
        super(message);
        this.code = code;
    }
}
