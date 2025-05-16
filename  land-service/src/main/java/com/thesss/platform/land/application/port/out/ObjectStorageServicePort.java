package com.thesss.platform.land.application.port.out;

import java.net.URL;

// This is a placeholder interface definition.
// Implementations will interact with the actual Object Storage service.
public interface ObjectStorageServicePort { // REQ-2-008, REQ-2-012

    /**
     * Generates a pre-signed URL for uploading a document.
     *
     * @param folder The folder within the bucket (e.g., "ownership-documents", "soil-test-reports").
     * @param fileName The desired file name.
     * @return A pre-signed URL allowing PUT requests.
     */
    URL generatePreSignedUploadUrl(String folder, String fileName); // Optional, if service handles uploads

    /**
     * Generates a pre-signed URL for viewing/downloading a document.
     *
     * @param storagePath The full path to the object in storage (e.g., "ownership-documents/doc123.pdf").
     * @return A pre-signed URL allowing GET requests.
     */
    URL generatePreSignedDownloadUrl(String storagePath); // REQ-2-008, REQ-2-012

    /**
     * Validates if a document exists at the given storage path.
     * This might be called during record creation/update if URLs are provided.
     *
     * @param storagePath The full path to the object in storage.
     * @return true if the object exists, false otherwise.
     */
    boolean doesObjectExist(String storagePath); // Optional validation step

    // Other methods might include deleting objects, etc.
}