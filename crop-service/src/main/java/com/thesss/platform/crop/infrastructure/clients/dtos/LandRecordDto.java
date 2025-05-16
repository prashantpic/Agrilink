package com.thesss.platform.crop.infrastructure.clients.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandRecordDto {
    private UUID landRecordId;
    private BigDecimal totalArea;
    private String areaUnit;
    private String status;
}