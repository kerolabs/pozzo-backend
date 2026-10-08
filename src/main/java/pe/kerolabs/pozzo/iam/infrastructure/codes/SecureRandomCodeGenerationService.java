package pe.kerolabs.pozzo.iam.infrastructure.codes;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.iam.domain.services.CodeGenerationService;

import java.security.SecureRandom;

/**
 * Generates six-digit codes with {@link SecureRandom} and hashes them with BCrypt.
 */
@Service
public class SecureRandomCodeGenerationService implements CodeGenerationService {

    private static final int CODE_BOUND = 1_000_000;

    private final SecureRandom random = new SecureRandom();
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String generate() {
        return "%06d".formatted(random.nextInt(CODE_BOUND));
    }

    @Override
    public String hash(String code) {
        return encoder.encode(code);
    }

    @Override
    public boolean matches(String code, String hash) {
        return code != null && encoder.matches(code, hash);
    }
}
