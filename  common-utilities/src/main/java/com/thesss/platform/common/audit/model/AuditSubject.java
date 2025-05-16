package com.thesss.platform.common.audit.model;

import java.util.Objects;

public class AuditSubject {

    private String id; // e.g., User ID, System process ID
    private String type; // e.g., USER, SYSTEM, API_KEY

    public AuditSubject() {
    }

    public AuditSubject(String id, String type) {
        this.id = id;
        this.type = type;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AuditSubject{" +
               "id='" + id + '\'' +
               ", type='" + type + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditSubject that = (AuditSubject) o;
        return Objects.equals(id, that.id) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }
}