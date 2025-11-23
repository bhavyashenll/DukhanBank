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
    
    /**
     * Transform bank response JSON to ApiResponse format for callback services (e.g., CRM.CREATE.TICKET)
     * @param fullResponse The full response JSON with status and bankResponse fields
     * @param serviceName The service name (e.g., "CRM.CREATE.TICKET")
     * @return Transformed ApiResponse with callback-specific format
     */
    ApiResponse<Map<String, Object>> transformBankResponseForProcessService(Map<String, Object> fullResponse, String serviceName);
}
