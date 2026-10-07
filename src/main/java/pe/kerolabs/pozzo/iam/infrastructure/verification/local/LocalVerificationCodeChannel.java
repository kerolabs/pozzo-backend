package pe.kerolabs.pozzo.iam.infrastructure.verification.local;

import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.SmsSender;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.verification.VerificationCodeChannel;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.services.CodeGenerationService;

/**
 * Pozzo generates the code, stores its hash and sends it with the configured {@link SmsSender}
 * (the server log or SMS Gate).
 */
@Component
public class LocalVerificationCodeChannel implements VerificationCodeChannel {

    private final CodeGenerationService codeGenerationService;
    private final SmsSender smsSender;

    public LocalVerificationCodeChannel(CodeGenerationService codeGenerationService, SmsSender smsSender) {
        this.codeGenerationService = codeGenerationService;
        this.smsSender = smsSender;
    }

    @Override
    public String deliver(PhoneNumber phoneNumber) {
        var code = codeGenerationService.generate();
        smsSender.send(phoneNumber, "Tu código de Pozzo es %s. Vence en %d minutos."
                .formatted(code, VerificationCode.VALIDITY.toMinutes()));
        return codeGenerationService.hash(code);
    }

    @Override
    public boolean check(PhoneNumber phoneNumber, String input, String storedValue) {
        return codeGenerationService.matches(input, storedValue);
    }
}
