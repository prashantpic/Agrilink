package com.thesss.platform.farmer.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerSearchApiRequest {

    private String name; // Search by any part of first, middle, or last name
    private String primaryPhoneNumber;
    private String nationalIdNumber; // Search by national ID number (if allowed and indexed appropriately)
    private String status; // Filter by a single status
    private List<String> statuses; // Filter by multiple statuses

    // Example location filter - can be expanded
    private String district;
    private String stateProvince;

    // Add other criteria as needed, e.g., registration date range
}