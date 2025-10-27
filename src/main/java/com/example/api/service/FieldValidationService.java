package com.example.api.service;

import com.example.api.dto.ApiResponse;

import java.util.Map;

/**
 * Interface for field validation service
 */
public interface FieldValidationService {

    /**
     * Validates the input request based on field configurations for the given screen
     * 
     * @param screenName the name of the screen to validate against
     * @param requestData the request data to validate
     * @return ApiResponse containing validation results
     */
    ApiResponse<Map<String, Object>> validateRequest(String screenName, Map<String, Object> requestData);

    /**
     * Checks if the validation response indicates a failed validation
     * 
     * @param validationResponse the validation response to check
     * @return true if validation failed, false otherwise
     */
    boolean isValidationFailed(ApiResponse<Map<String, Object>> validationResponse);
}
