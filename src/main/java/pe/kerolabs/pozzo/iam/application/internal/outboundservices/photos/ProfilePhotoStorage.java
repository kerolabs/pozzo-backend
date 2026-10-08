package pe.kerolabs.pozzo.iam.application.internal.outboundservices.photos;

import java.util.UUID;

/**
 * Outbound contract towards the storage of profile photos. The context only keeps the public URL of
 * the photo in the profile; the file lives in the storage service.
 */
public interface ProfilePhotoStorage {

    /**
     * Stores a photo under a new name, so a changed photo is never served from a cache.
     *
     * @param accountId   the account the photo belongs to
     * @param content     the image bytes
     * @param contentType the media type, e.g. image/jpeg
     * @return the public URL of the stored photo
     */
    String store(UUID accountId, byte[] content, String contentType);

    /**
     * Deletes a photo stored before. A URL this storage did not issue is ignored.
     *
     * @param publicUrl the public URL of the photo to be deleted
     */
    void delete(String publicUrl);
}
