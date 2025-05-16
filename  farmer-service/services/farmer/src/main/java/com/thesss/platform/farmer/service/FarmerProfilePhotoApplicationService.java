package com.thesss.platform.farmer.service;

import com.thesss.platform.farmer.domain.model.Farmer;
import com.thesss.platform.farmer.domain.model.FarmerId;
import com.thesss.platform.farmer.domain.repository.FarmerRepository;
import com.thesss.platform.farmer.exception.FarmerNotFoundException;
import com.thesss.platform.farmer.infrastructure.client.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmerProfilePhotoApplicationService {

    private final FarmerRepository farmerRepository;
    private final ObjectStorageService objectStorageService;

    @Value("${app.objectstorage.s3.bucket-name}")
    private String s3BucketName;

    @Transactional
    public String uploadProfilePhoto(UUID farmerIdValue, MultipartFile photo, String uploadingUserId) {
        log.info("Uploading profile photo for farmer ID: {} by user: {}", farmerIdValue, uploadingUserId);
        FarmerId farmerId = FarmerId.of(farmerIdValue);
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException(farmerIdValue));

        if (photo.isEmpty()) {
            throw new IllegalArgumentException("Profile photo file cannot be empty.");
        }

        String fileExtension = getFileExtension(photo.getOriginalFilename());
        String photoKey = String.format("farmers/%s/profile-photo/%s.%s",
                farmerId.getValue().toString(),
                System.currentTimeMillis(), // Or a more robust unique ID
                fileExtension);

        String photoUrl;
        try {
            photoUrl = objectStorageService.uploadFile(
                    s3BucketName,
                    photoKey,
                    photo.getInputStream(),
                    photo.getContentType()
            );
        } catch (IOException e) {
            log.error("Error uploading profile photo for farmer {}: {}", farmerIdValue, e.getMessage(), e);
            throw new RuntimeException("Failed to upload profile photo.", e);
        }

        farmer.setProfilePhotoUrl(photoUrl); // Assuming setter exists on Farmer domain object
        farmer.getAuditInfo().setLastUpdatedBy(uploadingUserId);
        farmer.getAuditInfo().setLastUpdatedDate(LocalDateTime.now());

        farmerRepository.save(farmer);
        log.info("Profile photo uploaded for farmer {}. URL: {}", farmerIdValue, photoUrl);
        return photoUrl;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "jpg"; // Default extension or throw error
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}