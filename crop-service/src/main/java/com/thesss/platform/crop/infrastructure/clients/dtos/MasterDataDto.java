package com.thesss.platform.crop.infrastructure.clients.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterDataDto {
    private String id; // Master Data Item Key
    private String value; // Display Value
    private String category; // Master Data Category
    private Map<String, Object> properties; // Additional properties like duration, conversion factor
}