package pe.kerolabs.pozzo.iam.domain.services;

/**
 * Domain service contract for generating cryptographically secure verification codes
 * and evaluating them against secure salted hashes.
 * The cleartext code is only dispatched to the recipient channel and never stored in persistence.
 */
public interface CodeGenerationService {

    /**
     * Generates a random six-digit numeric verification code.
     *
     * @return a six-digit string formatted code
     */
    String generate();

    /**
     * Computes a cryptographic one-way hash of the given code for persistent storage.
     *
     * @param code the cleartext code to hash
     * @return the secure hash representation
     */
    String hash(String code);

    /**
     * Validates whether a provided cleartext candidate code matches a previously stored hash.
     *
     * @param code the cleartext code submitted by the user
     * @param hash the stored cryptographic hash
     * @return true if the code matches the hash; false otherwise
     */
    boolean matches(String code, String hash);
}
