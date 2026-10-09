package pe.kerolabs.pozzo.support;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Clock for the tests that starts at the current time and only moves when a test advances it, so a rule
 * that depends on time, such as the 30 seconds before another SMS code, is tested without waiting.
 */
public class MutableClock extends Clock {

    /**
     * Starts on a whole millisecond: PostgreSQL keeps microseconds and rounds the rest, so a time with
     * nanoseconds could be stored a fraction later and a test that moves exactly ten minutes would land
     * just before the expiry instead of on it.
     */
    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.now().truncatedTo(ChronoUnit.MILLIS));

    public void advance(Duration duration) {
        now.updateAndGet(instant -> instant.plus(duration));
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(now.get(), zone);
    }

    @Override
    public Instant instant() {
        return now.get();
    }
}
