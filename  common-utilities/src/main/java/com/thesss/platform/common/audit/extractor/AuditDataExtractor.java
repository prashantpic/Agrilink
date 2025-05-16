package com.thesss.platform.common.audit.extractor;

import com.thesss.platform.common.audit.annotation.Auditable;
import com.thesss.platform.common.audit.model.AuditAction;
import com.thesss.platform.common.audit.model.AuditResource;
import com.thesss.platform.common.audit.model.AuditSubject;
import org.aspectj.lang.JoinPoint;

public interface AuditDataExtractor {

    /**
     * Extracts information about the subject (actor) performing the action
     * from the JoinPoint and the Auditable annotation.
     *
     * @param joinPoint The JoinPoint representing the method execution.
     * @param auditable The @Auditable annotation on the method or class.
     * @return An AuditSubject object, never null (can be an anonymous/system subject).
     */
    AuditSubject extractSubject(JoinPoint joinPoint, Auditable auditable);

    /**
     * Extracts information about the action performed from the JoinPoint
     * and the Auditable annotation.
     *
     * @param joinPoint The JoinPoint representing the method execution.
     * @param auditable The @Auditable annotation on the method or class.
     * @return An AuditAction object, never null.
     */
    AuditAction extractAction(JoinPoint joinPoint, Auditable auditable);

    /**
     * Extracts information about the resource affected by the action.
     * This can involve inspecting method arguments, return value, or exception.
     *
     * @param joinPoint The JoinPoint representing the method execution.
     * @param auditable The @Auditable annotation on the method or class.
     * @param result The result of the method execution (if successful), may be null.
     * @param exception The exception thrown during execution (if failed), may be null.
     * @return An AuditResource object. Can be an "empty" resource if no specific details are found/relevant.
     */
    AuditResource extractResource(JoinPoint joinPoint, Auditable auditable, Object result, Throwable exception);

    /**
     * Attempts to extract the client's IP address associated with the request
     * that triggered the JoinPoint.
     *
     * @param joinPoint The JoinPoint representing the method execution.
     * @return The client IP address as a String, or null if unavailable.
     */
    String extractClientIpAddress(JoinPoint joinPoint);
}