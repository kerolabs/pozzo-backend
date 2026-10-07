package pe.kerolabs.pozzo.savingsgroups.infrastructure.links;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationCode;

/**
 * Builds the invitation link that opens the application on the join screen.
 * The base URL is configurable so it can point to the domain verified for Android App Links.
 */
@Component
public class InvitationLinkBuilder {

    private final String baseUrl;

    public InvitationLinkBuilder(@Value("${invitations.link-base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public String linkFor(InvitationCode code) {
        return baseUrl + code.value().replace("-", "");
    }
}
