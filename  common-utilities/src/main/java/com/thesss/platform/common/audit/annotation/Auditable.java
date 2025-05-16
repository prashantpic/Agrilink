package com.thesss.platform.common.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The action name to be logged (e.g., "CREATE_USER", "UPDATE_PROFILE").
     * Defaults to the method name if not specified.
     */
    String action() default "";

    /**
     * Specifies a field name in the method arguments or return value
     * from which to extract the resource type.
     * Can use dot notation for nested fields (e.g., "requestDto.type").
     */
    String resourceTypeField() default "";

    /**
     * Specifies a field name in the method arguments or return value
     * from which to extract the resource ID.
     * Can use dot notation for nested fields (e.g., "user.id").
     */
    String resourceIdField() default "";

    /**
     * Specifies field names in the method arguments or return value
     * whose values represent the "old state" of the resource for diff logging.
     * Primarily useful if an argument represents the old state (e.g., in an update method).
     */
    String[] oldStateFields() default {};

    /**
     * Specifies field names in the method arguments or return value
     * whose values represent the "new state" of the resource for diff logging.
     * Typically used with arguments representing new data or the return value.
     */
    String[] newStateFields() default {};
}