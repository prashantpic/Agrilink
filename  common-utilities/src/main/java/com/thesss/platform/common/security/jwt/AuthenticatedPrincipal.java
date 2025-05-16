package com.thesss.platform.common.security.jwt;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AuthenticatedPrincipal {

    private final String userId;
    private final String username;
    private final List<String> roles;
    private final List<String> permissions;
    private final String tenantId; // Optional

    public AuthenticatedPrincipal(String userId, String username, List<String> roles, List<String> permissions, String tenantId) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.roles = roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
        this.permissions = permissions != null ? Collections.unmodifiableList(permissions) : Collections.emptyList();
        this.tenantId = tenantId;
    }

    // Getters

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public Optional<String> getTenantId() {
        return Optional.ofNullable(tenantId);
    }

    @Override
    public String toString() {
        return "AuthenticatedPrincipal{" +
               "userId='" + userId + '\'' +
               ", username='" + username + '\'' +
               ", roles=" + roles +
               ", permissions=" + permissions +
               ", tenantId=" + getTenantId().orElse("N/A") +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticatedPrincipal that = (AuthenticatedPrincipal) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(username, that.username) &&
               Objects.equals(roles, that.roles) &&
               Objects.equals(permissions, that.permissions) &&
               Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, roles, permissions, tenantId);
    }
}