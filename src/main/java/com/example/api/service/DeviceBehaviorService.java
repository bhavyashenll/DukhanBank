package com.example.api.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class DeviceBehaviorService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceBehaviorService.class);
    
    /**
     * Checks device ID and returns appropriate response based on device behavior
     * @param baseServiceRequest The request containing device info
     * @return ApiResponse with device-specific behavior or null for normal execution
     */
    public ApiResponse<Map<String, Object>> checkDeviceBehavior(BaseServiceRequest baseServiceRequest) {
        if (baseServiceRequest == null || baseServiceRequest.getDeviceInfo() == null) {
            logger.info("No device info provided - proceeding with normal execution");
            return null;
        }
        
        String deviceId = baseServiceRequest.getDeviceInfo().getDeviceId();
        logger.info("Checking device behavior for device ID: {}", deviceId);
        
        if ("device222".equalsIgnoreCase(deviceId)) {
            // Send MQ fail response
            logger.warn("Device ID {} - simulating MQ failure", deviceId);
            return ApiResponse.serviceUnavailable();
        } else if ("device333".equalsIgnoreCase(deviceId)) {
            // Send timeout response
            logger.warn("Device ID {} - simulating timeout", deviceId);
            return ApiResponse.requestTimeout();
        } else if ("device111".equalsIgnoreCase(deviceId)) {
            // Normal execution for device111
            logger.info("Device ID {} - normal execution", deviceId);
            return null;
        } else {
            // Unknown device ID - proceed with normal execution
            logger.info("Unknown device ID {} - proceeding with normal execution", deviceId);
            return null;
        }
    }
}
