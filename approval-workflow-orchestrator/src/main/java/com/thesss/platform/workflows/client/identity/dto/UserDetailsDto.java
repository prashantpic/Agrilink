package com.thesss.platform.workflows.client.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDto {
    private String userId;
    private String username;
    private String email;
    private List<String> roles;
    private Map<String, String> attributes; // e.g., "region", "department"
}