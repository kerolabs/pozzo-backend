package pe.kerolabs.pozzo.shared.interfaces.rest.transform;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.util.function.Function;

/**
 * Translates application {@link Result} values into HTTP responses.
 */
@NullMarked
public final class ResponseEntityAssembler {

    private ResponseEntityAssembler() {
    }

    /**
     * Converts a Result into an HTTP response: a success is mapped with the given resource
     * assembler and status, a failure is delegated to {@link ErrorResponseAssembler}.
     *
     * @param result                   the application result
     * @param successResourceAssembler maps the success value to the response body
     * @param successStatus            the HTTP status for a success
     * @param <T>                      the success value type
     * @param <R>                      the response body type
     * @return the response entity for the success or the failure
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromResult(
            Result<T, ApplicationError> result,
            Function<T, R> successResourceAssembler,
            HttpStatusCode successStatus) {
        return switch (result) {
            case Result.Success<T, ApplicationError> success ->
                    new ResponseEntity<>(successResourceAssembler.apply(success.value()), successStatus);
            case Result.Failure<T, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }

    /**
     * Converts a Result into an HTTP response without a body: {@code 204 No Content} for a success,
     * the error response for a failure.
     *
     * @param result the application result
     * @param <T>    the success value type, which is discarded
     * @return response entity for success or failure
     */
    public static <T> ResponseEntity<?> toNoContentResponseEntityFromResult(Result<T, ApplicationError> result) {
        return switch (result) {
            case Result.Success<T, ApplicationError> success -> ResponseEntity.noContent().build();
            case Result.Failure<T, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
