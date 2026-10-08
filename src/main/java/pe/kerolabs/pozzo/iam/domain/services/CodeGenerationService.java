package pe.kerolabs.pozzo.iam.domain.services;

/**
 * Generates verification codes and compares them with their hash.
 * The code in clear text is only sent to the member and never persisted.
 */
public interface CodeGenerationService {

    /**
     * Generates a random six-digit code.
     */
    String generate();

    /**
     * Hashes a code so it can be stored.
     */
    String hash(String code);

    /**
     * Returns true when the code corresponds to the hash.
     */
    boolean matches(String code, String hash);
}
