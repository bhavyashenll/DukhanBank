package com.example.api.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;

import java.util.Map;

public interface ProcessUIRequest {
    ApiResponse<Map<String, Object>> processEndToEndFlow(String serviceName, BaseServiceRequest baseServiceRequest, String screenName);

/**
     * Creates a new BaseServiceRequest with a path parameter added/overridden
     * @param originalRequest The original request
     * @param paramName The parameter name to add/override (can be null if no path param)
     * @param paramValue The parameter value from path variable (can be null if no path param)
     * @return Updated BaseServiceRequest (always returns a new instance)
     */
    BaseServiceRequest createRequestWithPathParam(BaseServiceRequest originalRequest, String paramName, String paramValue);    
}

