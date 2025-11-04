package com.example.api.service;

import com.example.api.dto.ApiResponse;
import java.util.Map;

public interface ProcessResponseService {
    
    /**
     * Transform bank response JSON to ApiResponse format based on service name
     * @param bankResponse The raw bank response JSON
     * @param serviceName The service name (e.g., "PROFIT.RATE")
     * @return Transformed ApiResponse
     */
    ApiResponse<Map<String, Object>> transformBankResponse(Map<String, Object> bankResponse, String serviceName);

    ApiResponse<Map<String, Object>> transformBankResponseForCallback(Map<String, Object> fullResponse, String serviceName);
}
