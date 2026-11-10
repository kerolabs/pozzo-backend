package pe.kerolabs.pozzo.support;

import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageLink;
import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageStorage;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the images of receipts in memory instead of Supabase Storage, so the tests can upload and link them.
 */
public class InMemoryReceiptImageStorage implements ReceiptImageStorage {

    private final Map<String, byte[]> images = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryReceiptImageStorage(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String store(UUID ownerId, byte[] content, String contentType) {
        var path = "%s/%s".formatted(ownerId, UUID.randomUUID());
        images.put(path, content.clone());
        return path;
    }

    @Override
    public ReceiptImageLink link(String path, Duration validity) {
        if (!images.containsKey(path)) {
            throw new IllegalArgumentException("No image at " + path);
        }
        return new ReceiptImageLink("https://storage.test/" + path, clock.instant().plus(validity));
    }
}
