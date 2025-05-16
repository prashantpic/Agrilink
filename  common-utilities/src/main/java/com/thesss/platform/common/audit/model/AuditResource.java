package com.thesss.platform.common.audit.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditResource {

    private String id;
    private String type;
    private Object oldValue;
    private Object newValue;

    public AuditResource() {
    }

    public AuditResource(String id, String type, Object oldValue, Object newValue) {
        this.id = id;
        this.type = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
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

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "AuditResource{" +
               (id != null ? "id='" + id + '\'' : "") +
               (type != null ? ", type='" + type + '\'' : "") +
               (oldValue != null ? ", oldValue=" + oldValue : "") +
               (newValue != null ? ", newValue=" + newValue : "") +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditResource that = (AuditResource) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(type, that.type) &&
               Objects.equals(oldValue, that.oldValue) &&
               Objects.equals(newValue, that.newValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, oldValue, newValue);
    }
}