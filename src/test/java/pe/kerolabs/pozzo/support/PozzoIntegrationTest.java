package pe.kerolabs.pozzo.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Starts the whole application against the PostgreSQL container, with MockMvc to call the REST API.
 *
 * <p>The properties replace whatever a local {@code .env} file sets, so a test never sends a real SMS,
 * email or push notification and never touches the production storage.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
        "authorization.jwt.secret=test-only-secret-for-the-pozzo-test-suite-0123456789abcdef",
        "sms.provider=log",
        "sms.test-numbers=",
        "sms.test-code=",
        "email.provider=log",
        "push.provider=log",
        "photos.supabase.url=",
        "photos.supabase.secret-key=",
        "notifications.dispatch-interval-ms=3600000"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public @interface PozzoIntegrationTest {
}
