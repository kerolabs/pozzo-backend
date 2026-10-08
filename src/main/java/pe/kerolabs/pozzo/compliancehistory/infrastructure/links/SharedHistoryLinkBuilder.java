package pe.kerolabs.pozzo.compliancehistory.infrastructure.links;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ShareToken;

/**
 * Builds the public URL of a shared history that the application hands to the system share sheet.
 * The base URL ends with the separator of the token, a slash for a path or an equals sign for a query
 * parameter; a slash is added when it ends with neither.
 */
@Component
public class SharedHistoryLinkBuilder {

    private final String baseUrl;

    public SharedHistoryLinkBuilder(@Value("${compliance.shared-link-base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") || baseUrl.endsWith("=") ? baseUrl : baseUrl + "/";
    }

    public String linkFor(ShareToken token) {
        return baseUrl + token.value();
    }
}
