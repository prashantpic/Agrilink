package com.thesss.platform.common.audit.extractor;

import com.thesss.platform.common.audit.annotation.Auditable;
import com.thesss.platform.common.audit.model.AuditAction;
import com.thesss.platform.common.audit.model.AuditResource;
import com.thesss.platform.common.audit.model.AuditSubject;
import com.thesss.platform.common.security.jwt.AuthenticatedPrincipal;
import com.thesss.platform.common.security.util.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class DefaultAuditDataExtractor implements AuditDataExtractor {

    private static final Logger log = LoggerFactory.getLogger(DefaultAuditDataExtractor.class);
    private final SecurityUtils securityUtils; // Keep for constructor injection / testability

    public DefaultAuditDataExtractor(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @Override
    public AuditSubject extractSubject(JoinPoint joinPoint, Auditable auditable) {
        Optional<AuthenticatedPrincipal> principalOpt = SecurityUtils.getCurrentPrincipal();
        if (principalOpt.isPresent()) {
            AuthenticatedPrincipal principal = principalOpt.get();
            return new AuditSubject(principal.getUserId(), "USER");
        }
        // Fallback: If no authenticated user, consider it a system action or anonymous
        // This could be enhanced based on application-specific needs, e.g., if system user ID is passed in args
        log.debug("No authenticated principal found for audit subject. Defaulting to SYSTEM/ANONYMOUS.");
        return new AuditSubject("ANONYMOUS", "SYSTEM");
    }

    @Override
    public AuditAction extractAction(JoinPoint joinPoint, Auditable auditable) {
        String actionName = auditable.action();
        if (actionName == null || actionName.trim().isEmpty()) {
            Signature signature = joinPoint.getSignature();
            actionName = signature.getName(); // Default to method name
            log.debug("Auditable action name not specified, defaulting to method name: {}", actionName);
        }
        // Category could be derived or configured if needed
        return new AuditAction(actionName.toUpperCase()); // Convention: uppercase action names
    }

    @Override
    public AuditResource extractResource(JoinPoint joinPoint, Auditable auditable, Object result, Throwable exception) {
        String resourceId = null;
        String resourceType = null;
        Map<String, Object> oldState = Collections.emptyMap();
        Map<String, Object> newState = Collections.emptyMap();

        // Extract from arguments first
        Object primaryTarget = findPrimaryAuditableObject(joinPoint.getArgs(), auditable);
        if (primaryTarget == null && result != null) { // If not found in args, check result
            primaryTarget = result;
        }

        if (primaryTarget != null) {
            resourceId = extractFieldValue(primaryTarget, auditable.resourceIdField()).map(String::valueOf).orElse(null);
            resourceType = extractFieldValue(primaryTarget, auditable.resourceTypeField()).map(String::valueOf).orElse(primaryTarget.getClass().getSimpleName());

            if (auditable.oldStateFields().length > 0) {
                oldState = extractFieldsAsMap(primaryTarget, auditable.oldStateFields());
            }
            if (auditable.newStateFields().length > 0) {
                 // If `newStateFields` are specified, we assume `primaryTarget` (which could be args or result)
                 // represents the new state.
                newState = extractFieldsAsMap(primaryTarget, auditable.newStateFields());
            } else if (result != null && primaryTarget == result) {
                // If newStateFields are not specified and we are using the result as primary target,
                // consider the entire result (or relevant parts) as new state.
                // This might need a more sophisticated approach for complex objects.
                // For now, if no newStateFields, and primaryTarget is result, new state is simplified.
                if (auditable.resourceIdField().isEmpty() && auditable.resourceTypeField().isEmpty()) {
                    newState = Collections.singletonMap("value", result.toString()); // Simplified
                }
            }
        }


        // If resourceType is still null, try to infer from method/class if possible, or leave null
        if (resourceType == null) {
            resourceType = joinPoint.getSignature().getDeclaringType().getSimpleName();
            log.debug("Resource type not explicitly extracted, defaulting to declaring type: {}", resourceType);
        }

        // Handle scenarios where the resource ID might be directly in an argument, not a field of an object
        if (resourceId == null && !auditable.resourceIdField().isEmpty()) {
            resourceId = findNamedArgument(joinPoint, auditable.resourceIdField()).map(String::valueOf).orElse(null);
        }
        if (resourceType == null && !auditable.resourceTypeField().isEmpty()) {
            resourceType = findNamedArgument(joinPoint, auditable.resourceTypeField()).map(String::valueOf).orElse(resourceType);
        }

        return new AuditResource(resourceId, resourceType, oldState.isEmpty() ? null : oldState, newState.isEmpty() ? null : newState);
    }

    private Object findPrimaryAuditableObject(Object[] args, Auditable auditable) {
        // Prioritize object containing resourceIdField or resourceTypeField if specified
        if (!auditable.resourceIdField().isEmpty() || !auditable.resourceTypeField().isEmpty()) {
            for (Object arg : args) {
                if (arg == null) continue;
                boolean hasIdField = !auditable.resourceIdField().isEmpty() && hasFieldOrGetter(arg, auditable.resourceIdField());
                boolean hasTypeField = !auditable.resourceTypeField().isEmpty() && hasFieldOrGetter(arg, auditable.resourceTypeField());
                if (hasIdField || hasTypeField) {
                    return arg;
                }
            }
        }
        // If no specific fields, and args exist, pick the first non-null complex object? Or first arg?
        // This heuristic might need refinement. For simplicity, if multiple args, it's hard to guess.
        // If only one arg that's not primitive, it's a good candidate.
        if (args.length == 1 && args[0] != null && !args[0].getClass().isPrimitive() && !(args[0] instanceof String)) {
            return args[0];
        }
        return null; // Could not determine a primary object from args
    }

    private Optional<Object> findNamedArgument(JoinPoint joinPoint, String paramName) {
        if (paramName == null || paramName.isEmpty()) return Optional.empty();
        Signature sig = joinPoint.getSignature();
        if (sig instanceof MethodSignature methodSignature) {
            String[] paramNames = methodSignature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < paramNames.length; i++) {
                if (paramName.equals(paramNames[i])) {
                    return Optional.ofNullable(args[i]);
                }
            }
        }
        return Optional.empty();
    }

    private boolean hasFieldOrGetter(Object obj, String fieldName) {
        if (obj == null || fieldName == null || fieldName.isEmpty()) return false;
        // Check getter
        try {
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            obj.getClass().getMethod(getterName);
            return true;
        } catch (NoSuchMethodException e) {
            try {
                String isGetterName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                obj.getClass().getMethod(isGetterName);
                return true;
            } catch (NoSuchMethodException e2) {
                // Check field
                try {
                    obj.getClass().getDeclaredField(fieldName); // Check declared fields too
                    return true;
                } catch (NoSuchFieldException e3) {
                    return false;
                }
            }
        }
    }


    private Optional<Object> extractFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null || fieldName.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            // Try getter first
            String getterMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method getter = obj.getClass().getMethod(getterMethodName);
            return Optional.ofNullable(getter.invoke(obj));
        } catch (NoSuchMethodException e) {
            // Try boolean getter if no standard getter
            try {
                String isGetterMethodName = "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Method isGetter = obj.getClass().getMethod(isGetterMethodName);
                return Optional.ofNullable(isGetter.invoke(obj));
            } catch (NoSuchMethodException e2) {
                // Try direct field access (including private, requires setAccessible(true))
                try {
                    Field field = findField(obj.getClass(), fieldName);
                    field.setAccessible(true);
                    return Optional.ofNullable(field.get(obj));
                } catch (NoSuchFieldException e3) {
                    log.trace("Field or getter '{}' not found on object of type {}", fieldName, obj.getClass().getName());
                } catch (IllegalAccessException e3) {
                    log.warn("Cannot access field '{}' on object of type {}", fieldName, obj.getClass().getName(), e3);
                }
            } catch (IllegalAccessException | InvocationTargetException e2) {
                 log.warn("Boolean getter for '{}' on {} threw an exception or was inaccessible.", fieldName, obj.getClass().getName(), e2);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("Getter for '{}' on {} threw an exception or was inaccessible.", fieldName, obj.getClass().getName(), e);
        }
        return Optional.empty();
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + fieldName + " not found in class " + clazz.getName() + " or its superclasses.");
    }

    private Map<String, Object> extractFieldsAsMap(Object obj, String[] fieldNames) {
        if (obj == null || fieldNames == null || fieldNames.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, Object> values = new HashMap<>();
        for (String fieldName : fieldNames) {
            if (fieldName != null && !fieldName.trim().isEmpty()) {
                extractFieldValue(obj, fieldName.trim()).ifPresent(value -> values.put(fieldName.trim(), value));
            }
        }
        return values;
    }

    @Override
    public String extractClientIpAddress(JoinPoint joinPoint) {
        return SecurityUtils.getCurrentHttpRequest()
                .map(SecurityUtils::getClientIpAddress)
                .orElseGet(() -> {
                    log.debug("HttpServletRequest not available, cannot extract client IP for audit.");
                    return null;
                });
    }
}