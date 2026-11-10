package pe.kerolabs.pozzo.contributions.infrastructure.links;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.VerificationToken;

/**
 * Builds the public URL that opens the record of a shared contribution.
 */
@Component
public class VerifiedContributionLinkBuilder {

    private final String baseUrl;

    public VerifiedContributionLinkBuilder(@Value("${contributions.shared-link-base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") || baseUrl.endsWith("=") ? baseUrl : baseUrl + "/";
    }

    public String linkFor(VerificationToken token) {
        return baseUrl + token.value();
    }
}
