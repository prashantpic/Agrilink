package com.thesss.platform.workflows.client.identity;

import com.thesss.platform.workflows.client.identity.dto.UserDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "identity-access-service", path = "/api/internal/users")
public interface IdentityAccessServiceClient {

    @GetMapping("/{userId}")
    UserDetailsDto getUserDetails(
            @PathVariable("userId") String userId);

    @GetMapping("/search/by-role-region")
    List<UserDetailsDto> findUsersByRoleAndRegion(
            @RequestParam("roleName") String roleName,
            @RequestParam(value = "region", required = false) String region);
}