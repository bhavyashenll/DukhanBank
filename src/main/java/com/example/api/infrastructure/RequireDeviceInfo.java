package com.example.api.infrastructure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark controllers that require DeviceInfoDto as mandatory request parameter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireDeviceInfo {
    String value() default "Device information is required for this endpoint";
}
