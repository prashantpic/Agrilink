package com.thesss.platform.common.audit.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditAction {

    private String name;
    private String category; // e.g., AUTHENTICATION, DATA_MODIFICATION

    public AuditAction() {
    }

    public AuditAction(String name) {
        this.name = name;
    }

    public AuditAction(String name, String category) {
        this.name = name;
        this.category = category;
    }

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "AuditAction{" +
               "name='" + name + '\'' +
               (category != null ? ", category='" + category + '\'' : "") +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditAction that = (AuditAction) o;
        return Objects.equals(name, that.name) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category);
    }
}