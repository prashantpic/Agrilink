package com.thesss.platform.crop.infrastructure.clients.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerDto {
    private UUID farmerId;
    private String fullName;
    private String status;
}