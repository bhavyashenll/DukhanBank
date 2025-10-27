package com.example.api.infrastructure;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.dto.UsernameSuggestionRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Ultra-efficient aspect for @RequireDeviceInfo class annotation
 */
@Aspect
@Component
public class DeviceInfoValidationAspect {

    @Around("@within(com.example.api.infrastructure.RequireDeviceInfo)")
    public Object validateDeviceInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BaseServiceRequest request && request.getDeviceInfo() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.deviceInfoNotFound());
            }

            if (arg instanceof UsernameSuggestionRequest usernameRequest && usernameRequest.getDeviceInfo() == null) {
                return ResponseEntity.badRequest().body(ApiResponse.deviceInfoNotFound());
            }
        }
        return joinPoint.proceed();
    }
}
