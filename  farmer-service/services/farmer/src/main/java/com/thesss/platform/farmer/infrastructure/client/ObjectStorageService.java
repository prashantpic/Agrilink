package com.thesss.platform.farmer.infrastructure.client;

import java.io.InputStream;
// Consider using MultipartFile directly if it simplifies things and if this service is only used with Spring MVC
// import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {

    /**
     * Uploads a file to the object storage.
     *
     * @param bucketName  The name of the bucket.
     * @param key         The key (path and filename) for the object in the bucket.
     * @param inputStream The InputStream of the file content.
     * @param contentType The MIME type of the file.
     * @return The public URL or a unique identifier/key of the uploaded object.
     */
    String uploadFile(String bucketName, String key, InputStream inputStream, String contentType);

    /**
     * Downloads a file from the object storage.
     *
     * @param bucketName The name of the bucket.
     * @param key        The key of the object to download.
     * @return An InputStream of the file content.
     */
    InputStream downloadFile(String bucketName, String key);

    /**
     * Deletes a file from the object storage.
     *
     * @param bucketName The name of the bucket.
     * @param key        The key of the object to delete.
     */
    void deleteFile(String bucketName, String key);

    // Optional: Method to upload using MultipartFile directly if preferred
    // String uploadFile(String bucketName, String key, MultipartFile file);
}