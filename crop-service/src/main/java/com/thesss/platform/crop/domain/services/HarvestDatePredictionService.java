package com.thesss.platform.crop.domain.services;

import com.thesss.platform.crop.application.ports.outgoing.MasterDataServicePort;
import com.thesss.platform.crop.domain.exceptions.MasterDataResolutionException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class HarvestDatePredictionService {

    public LocalDate predictExpectedHarvestDate(String cropMasterId, String varietyMasterIdOrText, LocalDate sowingDate, MasterDataServicePort masterDataServicePort) {
        Objects.requireNonNull(cropMasterId, "Crop master ID cannot be null");
        Objects.requireNonNull(sowingDate, "Sowing date cannot be null");
        Objects.requireNonNull(masterDataServicePort, "MasterDataServicePort cannot be null");

        Integer durationDays = masterDataServicePort.getCropDurationDays(cropMasterId, varietyMasterIdOrText)
            .orElseThrow(() -> new MasterDataResolutionException(
                "Crop duration not found for crop: " + cropMasterId +
                (varietyMasterIdOrText != null ? ", variety: " + varietyMasterIdOrText : "")
            ));

        if (durationDays < 0) {
             throw new MasterDataResolutionException("Invalid crop duration (" + durationDays + ") received from Master Data Service.");
        }

        return sowingDate.plusDays(durationDays);
    }
}