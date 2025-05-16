package com.thesss.platform.common.logging.util;

import org.slf4j.MDC;

public final class MDCContext {

    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String USER_ID_KEY = "userId";
    public static final String TENANT_ID_KEY = "tenantId"; // Added as per previous thoughts

    private MDCContext() {
        // restrict instantiation
    }

    /**
     * Sets the correlation ID in the MDC. If null, removes the key.
     *
     * @param correlationId The correlation ID to set.
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        } else {
            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    /**
     * Retrieves the correlation ID from the MDC.
     *
     * @return The correlation ID, or null if not present.
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    /**
     * Sets the user ID in the MDC. If null, removes the key.
     *
     * @param userId The user ID to set.
     */
    public static void setUserId(String userId) {
        if (userId != null) {
            MDC.put(USER_ID_KEY, userId);
        } else {
            MDC.remove(USER_ID_KEY);
        }
    }

    /**
     * Retrieves the user ID from the MDC.
     *
     * @return The user ID, or null if not present.
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }

    /**
     * Sets the tenant ID in the MDC. If null, removes the key.
     *
     * @param tenantId The tenant ID to set.
     */
    public static void setTenantId(String tenantId) {
        if (tenantId != null) {
            MDC.put(TENANT_ID_KEY, tenantId);
        } else {
            MDC.remove(TENANT_ID_KEY);
        }
    }

    /**
     * Retrieves the tenant ID from the MDC.
     *
     * @return The tenant ID, or null if not present.
     */
    public static String getTenantId() {
        return MDC.get(TENANT_ID_KEY);
    }


    /**
     * Puts an arbitrary key-value pair into the MDC. If value is null, removes the key.
     *
     * @param key The key.
     * @param value The value.
     */
    public static void put(String key, String value) {
         if (key == null) {
             return; // Do not attempt to put a null key
         }
         if (value != null) {
             MDC.put(key, value);
         } else {
             MDC.remove(key);
         }
    }

    /**
     * Clears all entries from the MDC for the current thread.
     * Should be called in a finally block to prevent context leakage.
     */
    public static void clear() {
        MDC.clear();
    }
}