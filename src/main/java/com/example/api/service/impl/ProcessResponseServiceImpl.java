package com.example.api.service.impl;

import com.example.api.service.ProcessResponseService;
import com.example.api.dto.ApiResponse;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.Objects;

@Service
public class ProcessResponseServiceImpl implements ProcessResponseService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessResponseServiceImpl.class);

    @Override
    public ApiResponse<Map<String, Object>> transformBankResponse(Map<String, Object> bankResponse, String serviceName) {
        logger.debug("Transforming bank response for service: {}", serviceName);
        
        if (Objects.isNull(bankResponse)) {
            logger.warn("Bank response is null for service: {}", serviceName);
            return ApiResponse.error();
        }

        try {
            // Convert service name to camelCase (e.g., "PROFIT.RATE" -> "profitRate")
            String dataKey = convertToCamelCase(serviceName);
            logger.debug("Looking for data key: {} in bank response", dataKey);
            
            // Extract the data array from bankResponse
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) bankResponse.get(dataKey);
            
            if (Objects.isNull(dataList)) {
                logger.warn("No data found for key: {} in bank response", dataKey);
                return ApiResponse.noDataFound();
            }
            
            logger.info("Successfully transformed {} items for service: {}", dataList.size(), serviceName);
            return ApiResponse.success(dataList);
            
        } catch (Exception e) {
            logger.error("Error transforming bank response for service: {}", serviceName, e);
            return ApiResponse.error();
        }
    }
    
    /**
     * Convert service name to camelCase
     * @param serviceName Service name like "PROFIT.RATE"
     * @return camelCase version like "profitRate"
     */
    private String convertToCamelCase(String serviceName) {
        if (Objects.isNull(serviceName) || serviceName.trim().isEmpty()) {
            return serviceName;
        }
        
        String[] parts = serviceName.split("\\.");
        if (parts.length == 0) {
            return serviceName;
        }
        
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                result.append(parts[i].substring(0, 1).toUpperCase())
                      .append(parts[i].substring(1).toLowerCase());
            }
        }
        
        return result.toString();
    }
}
