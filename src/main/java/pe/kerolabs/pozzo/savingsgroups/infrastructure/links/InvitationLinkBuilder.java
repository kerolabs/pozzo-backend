package pe.kerolabs.pozzo.savingsgroups.infrastructure.links;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationCode;

/**
 * Builds the invitation link: a page that shows the code and how to join from the application.
 * The base URL is configurable so it can point to the domain verified for Android App Links. It ends with
 * the separator of the code, a slash for a path or an equals sign for a query parameter; a slash is added
 * when it ends with neither.
 */
@Component
public class InvitationLinkBuilder {

    private final String baseUrl;

    public InvitationLinkBuilder(@Value("${invitations.link-base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") || baseUrl.endsWith("=") ? baseUrl : baseUrl + "/";
    }

    public String linkFor(InvitationCode code) {
        return baseUrl + code.value().replace("-", "");
    }
}
