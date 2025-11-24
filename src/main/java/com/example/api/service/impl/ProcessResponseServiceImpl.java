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
        logger.info("Transforming bank response for service: {} {} ", serviceName, bankResponse);
        
        if (Objects.isNull(bankResponse)) {
            logger.warn("Bank response is null for service: {}", serviceName);
            return ApiResponse.error();
        }

        try {
            String dataKey;
            if(serviceName.equalsIgnoreCase("ACCOUNT.DETAIL")) {
                dataKey = "depAcctInfo";
            } else if(serviceName.equalsIgnoreCase("TRANSACTION.STATEMENT")) {
                dataKey = "transactions";
            } else if (serviceName.equalsIgnoreCase("SIGN.ON")) {
                dataKey = "accounts";
            } else {
                // Convert service name to camelCase (e.g., "PROFIT.RATE" -> "profitRate")
                dataKey = convertToCamelCase(serviceName);
            }
            logger.info("Looking for data key: {} in bank response", dataKey);
            
            // Extract the data from bankResponse
            Object dataObject = bankResponse.get(dataKey);
            
            if (Objects.isNull(dataObject)) {
                logger.warn("No data found for key: {} in bank response", dataKey);
                return ApiResponse.noDataFound();
            }
            
            // Handle both List and single Map cases for ACCOUNT.DETAIL
            List<Map<String, Object>> dataList;
            if (serviceName.equalsIgnoreCase("ACCOUNT.DETAIL") && dataObject instanceof Map) {
                // For ACCOUNT.DETAIL, depAcctInfo is a single Map, wrap it in a List
                @SuppressWarnings("unchecked")
                Map<String, Object> singleMap = (Map<String, Object>) dataObject;
                dataList = List.of(singleMap);
            } else if (serviceName.equalsIgnoreCase("SIGN.ON") && dataObject instanceof List) {
                // For SIGN.ON, accounts is already a List
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> accountsList = (List<Map<String, Object>>) dataObject;
                dataList = accountsList;
            } else if (serviceName.equalsIgnoreCase("TRANSACTION.STATEMENT") && dataObject instanceof List) {
                // For TRANSACTION.STATEMENT, transactions is already a List
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> transactionsList = (List<Map<String, Object>>) dataObject;
                dataList = transactionsList;
            } else {
                // For other services, expect a List
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) dataObject;
                dataList = list;
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

    @Override
    public ApiResponse<Map<String, Object>> transformBankResponseForProcessService(Map<String, Object> fullResponse, String serviceName) {
        logger.info("Transforming bank response for process service: {} {}", serviceName, fullResponse);
        
        if (Objects.isNull(fullResponse)) {
            logger.warn("Full response is null for process service: {}", serviceName);
            return ApiResponse.serviceUnavailable();
        }
        
        try {
            String status = (String) fullResponse.get("status");
            @SuppressWarnings("unchecked")
            Map<String, Object> bankResponse = (Map<String, Object>) fullResponse.get("bankResponse");
            
            // Handle ERROR status
            if (Objects.nonNull(status) && status.equalsIgnoreCase("ERROR")) {
                logger.info("Error case: status is ERROR for service: {}", serviceName);
                return ApiResponse.serviceUnavailable();
            }
            
            // Handle SUCCESS status
            if (Objects.nonNull(status) && status.equalsIgnoreCase("SUCCESS")) {
                if (Objects.isNull(bankResponse)) {
                    logger.warn("SUCCESS status but bankResponse is null for service: {}", serviceName);
                    return ApiResponse.badRequest();
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Object> returnStatus = (Map<String, Object>) bankResponse.get("returnStatus");
                
                if (Objects.isNull(returnStatus)) {
                    logger.warn("SUCCESS status but returnStatus is null for service: {}", serviceName);
                    return ApiResponse.badRequest();
                }
                
                String returnCode = (String) returnStatus.get("returnCode");
                
                // Success case: returnCode = "0000"
                if ("0000".equals(returnCode)) {
                    ApiResponse<Map<String, Object>> response = new ApiResponse<>();
                    response.setStatus(new ApiResponse.Status("000000", "Successfully processed"));

                    if(serviceName.equalsIgnoreCase("CRM.CREATE.TICKET") || serviceName.equalsIgnoreCase("CRM.CREATE.LEAD")) {
                        // Extract ticketRefNumber from bankResponse for both CREATE.TICKET and CREATE.LEAD
                        String ticketRefNumber = (String) bankResponse.get("ticketRefNumber");
                        logger.info("Success case: ticketRefNumber = {} for service: {}", ticketRefNumber, serviceName);
                        Map<String, Object> data = new java.util.HashMap<>();
                        data.put("ticketRefNumber", ticketRefNumber);
                        response.setData(List.of(data));
                    }
                    
                    return response;
                } else {
                    // Bad request: returnCode is not "0000"
                    logger.info("Bad request case: returnCode = {} for service: {}", returnCode, serviceName);
                    return ApiResponse.badRequest();
                }
            }
            
            // Default case: unexpected status or null status
            logger.warn("Unexpected status '{}' in transformBankResponseForProcessService for service: {}", status, serviceName);
            return ApiResponse.badRequest();
            
        } catch (Exception e) {
            logger.error("Error transforming bank response for process service: {}", serviceName, e);
            return ApiResponse.badRequest();
        }
    }
}

