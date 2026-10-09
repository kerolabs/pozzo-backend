package pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShareLink: the public link to a member's history")
class ShareLinkTest {

    private static final Instant NOW = Instant.parse("2026-10-08T15:00:00Z");

    @Test
    void worksForSevenDays() {
        var link = ShareLink.issue(UUID.randomUUID(), NOW);

        assertThat(link.isValid(NOW.plus(Duration.ofDays(7)).minusSeconds(1))).isTrue();
        assertThat(link.isValid(NOW.plus(Duration.ofDays(7)))).isFalse();
    }

    @Test
    void stopsWorkingWhenRevoked() {
        var link = ShareLink.issue(UUID.randomUUID(), NOW);

        link.revoke();

        assertThat(link.isValid(NOW.plusSeconds(60))).isFalse();
    }

    @Test
    void everyLinkHasItsOwnToken() {
        var member = UUID.randomUUID();

        assertThat(ShareLink.issue(member, NOW).getToken()).isNotEqualTo(ShareLink.issue(member, NOW).getToken());
    }
}
