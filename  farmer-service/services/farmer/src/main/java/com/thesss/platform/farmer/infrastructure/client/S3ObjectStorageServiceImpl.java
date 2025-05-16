package com.thesss.platform.farmer.infrastructure.client;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


@Service
public class S3ObjectStorageServiceImpl implements ObjectStorageService {

    private final S3Client s3Client;
    private final S3Template s3Template; // Spring Cloud AWS S3Template for convenience

    @Value("${app.objectstorage.s3.bucket-name}")
    private String defaultBucketName;

    public S3ObjectStorageServiceImpl(S3Client s3Client, S3Template s3Template) {
        this.s3Client = s3Client;
        this.s3Template = s3Template;
    }

    @Override
    public String uploadFile(String bucketName, String key, InputStream inputStream, String contentType) {
        final String targetBucket = StringUtils.hasText(bucketName) ? bucketName : defaultBucketName;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, inputStream.available()));

            // Construct the URL. This assumes the bucket is public or presigned URLs are used if private.
            // For simplicity, constructing a standard public URL.
             GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .build();
            URL url = s3Client.utilities().getUrl(getUrlRequest);
            return url.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + key, e);
        }
    }
    
    @Override
    public S3Resource downloadFile(String bucketName, String key) {
         final String targetBucket = StringUtils.hasText(bucketName) ? bucketName : defaultBucketName;
        // S3Template provides a convenient way to get a Spring Resource
        return s3Template.download(targetBucket, key);
    }


    @Override
    public InputStream downloadFileAsStream(String bucketName, String key) {
        final String targetBucket = StringUtils.hasText(bucketName) ? bucketName : defaultBucketName;
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .build();
        return s3Client.getObject(getObjectRequest);
    }


    @Override
    public void deleteFile(String bucketName, String key) {
        final String targetBucket = StringUtils.hasText(bucketName) ? bucketName : defaultBucketName;
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }
}