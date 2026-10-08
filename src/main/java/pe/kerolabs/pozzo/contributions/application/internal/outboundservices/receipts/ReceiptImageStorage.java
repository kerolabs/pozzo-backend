package pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts;

import java.time.Duration;
import java.util.UUID;

/**
 * Outbound contract towards the private storage of receipt images. The contribution only keeps the path
 * of its image; whoever may see it gets a link that expires.
 */
public interface ReceiptImageStorage {

    /**
     * Stores the image of a receipt under a new name.
     *
     * @param contributionId the contribution the receipt proves
     * @param content        the image bytes
     * @param contentType    the media type, e.g. image/jpeg
     * @return the path of the image inside the storage
     */
    String store(UUID contributionId, byte[] content, String contentType);

    /**
     * Returns a link to a stored image that works for a limited time.
     *
     * @param path     the path returned by {@link #store}
     * @param validity how long the link works
     */
    ReceiptImageLink link(String path, Duration validity);
}
