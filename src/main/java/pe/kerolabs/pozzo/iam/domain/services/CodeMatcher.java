package pe.kerolabs.pozzo.iam.domain.services;

/**
 * Decides whether the code typed by a member is the one that was sent. It is only asked when the
 * code is still pending and has not expired, so a matcher that calls an external service is not
 * called needlessly.
 */
@FunctionalInterface
public interface CodeMatcher {

    boolean matches(String input);
}
