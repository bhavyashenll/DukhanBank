package com.example.api.account.service.impl;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.account.service.AccountTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AccountTransactionServiceImpl implements AccountTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(AccountTransactionServiceImpl.class);
    private static final String SUCCESS_CODE = "000000";
    private static final String SUCCESS_DESCRIPTION = "Successfully processed";

    @Override
    public ApiResponse<Map<String, Object>> validateRequest(BaseServiceRequest baseServiceRequest) {
        logger.info("Validating account transaction request");
        
        if (Objects.isNull(baseServiceRequest) || Objects.isNull(baseServiceRequest.getRequestInfo())) {
            logger.warn("Request or requestInfo is null");
            ApiResponse<Map<String, Object>> response = new ApiResponse<>();
            response.setStatus(new ApiResponse.Status("000400", "Bad request"));
            response.setData(new ArrayList<>());
            return response;
        }
        
        Map<String, Object> requestInfo = baseServiceRequest.getRequestInfo();
        List<String> errors = new ArrayList<>();
        
        Object customerId = requestInfo.get("customerId");
        if (Objects.isNull(customerId) || (customerId.toString().trim().isEmpty())) {
            errors.add("customerId is required");
        }
        
        Object acctNumber = requestInfo.get("acctNumber");
        if (Objects.isNull(acctNumber) || (acctNumber.toString().trim().isEmpty())) {
            errors.add("acctNumber is required");
        }
        
        Object transactionType = requestInfo.get("transactionType");
        if (Objects.isNull(transactionType) || (transactionType.toString().trim().isEmpty())) {
            errors.add("transactionType is required");
        }
        
        if (!errors.isEmpty()) {
            logger.warn("Validation failed: missing required fields: {}", errors);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>();
            response.setStatus(new ApiResponse.Status("000400", "Bad request"));
            response.setData(new ArrayList<>());
            return response;
        }
        
        logger.info("Request validation successful");
        return null;
    }

    @Override
    public ApiResponse<Map<String, Object>> postProcessAccountTransactions(
            ApiResponse<Map<String, Object>> response, 
            BaseServiceRequest baseServiceRequest) {
        logger.info("Post-processing account transactions response with transactionType filter");

        try {
            if (Objects.isNull(response) || Objects.isNull(response.getStatus()) || Objects.isNull(response.getData())) {
                logger.info("Response is null or missing data, returning empty status");
                return emptyWithStatus(response);
            }

            String code = response.getStatus().getCode();
            if (!SUCCESS_CODE.equals(code)) {
                logger.info("Response status code is not success: {}", code);
                return emptyWithStatus(response);
            }

            // Get transactionType from request
            String requestedTransactionType = getTransactionTypeFromRequest(baseServiceRequest);
            if (Objects.isNull(requestedTransactionType) || requestedTransactionType.trim().isEmpty()) {
                logger.warn("transactionType not found in request, returning all transactions");
            }

            List<Map<String, Object>> dataList = response.getData();
            logger.info("Response data list size: {}", Objects.nonNull(dataList) ? dataList.size() : 0);
            
            if (Objects.isNull(dataList) || dataList.isEmpty()) {
                logger.info("No transaction data found in response");
                return emptyWithStatus(response);
            }

            // If transactionType is "any" (case insensitive), return all transactions without filtering
            List<Map<String, Object>> filteredTransactions;
            if (Objects.nonNull(requestedTransactionType) && "any".equalsIgnoreCase(requestedTransactionType.trim())) {
                logger.info("transactionType is 'any', returning all transactions without filtering");
                filteredTransactions = extractAndFilterTransactions(dataList, null);
            } else {
                // Extract and filter transactions - preserve all fields from processEndToEndFlow
                filteredTransactions = extractAndFilterTransactions(dataList, requestedTransactionType);
            }

            logger.info("Successfully processed {} transactions", filteredTransactions.size());
            ApiResponse<Map<String, Object>> result = new ApiResponse<>();
            result.setStatus(new ApiResponse.Status(SUCCESS_CODE, SUCCESS_DESCRIPTION));
            result.setData(filteredTransactions);
            return result;

        } catch (Exception e) {
            logger.error("Error post-processing account transactions response", e);
            return ApiResponse.error();
        }
    }

    private String getTransactionTypeFromRequest(BaseServiceRequest baseServiceRequest) {
        if (Objects.isNull(baseServiceRequest) || Objects.isNull(baseServiceRequest.getRequestInfo())) {
            return null;
        }
        Object transactionType = baseServiceRequest.getRequestInfo().get("transactionType");
        return Objects.nonNull(transactionType) ? transactionType.toString().trim() : null;
    }

    private List<Map<String, Object>> extractAndFilterTransactions(List<Map<String, Object>> dataList, String requestedTransactionType) {
        List<Map<String, Object>> transactions = new ArrayList<>();

        logger.debug("Extracting transactions from dataList with {} items", dataList.size());

        // Process each data item in the response
        for (Map<String, Object> data : dataList) {
            logger.debug("Processing data item, keys: {}", data.keySet());
            
            // Check if the data item itself is a transaction (has transactionDesc or transactionType field)
            if (data.containsKey("transactionDesc") || data.containsKey("transactionType")) {
                logger.debug("Data item appears to be a transaction object directly");
                // Filter by transactionType if requested
                if (shouldIncludeTransaction(data, requestedTransactionType)) {
                    transactions.add(new HashMap<>(data)); // Create a copy to avoid modifying original
                }
                continue;
            }
            
            // Check if the data contains transaction array in arrayMsg
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> transactionList = (List<Map<String, Object>>) data.get("arrayMsg");
            
            if (Objects.nonNull(transactionList) && !transactionList.isEmpty()) {
                logger.debug("Found arrayMsg with {} transactions", transactionList.size());
                for (Map<String, Object> transactionMap : transactionList) {
                    if (shouldIncludeTransaction(transactionMap, requestedTransactionType)) {
                        transactions.add(new HashMap<>(transactionMap)); // Preserve all fields
                    }
                }
            }
            
            // Also check for transactions in other possible structures (e.g., transactions.transaction array)
            @SuppressWarnings("unchecked")
            Map<String, Object> transactionsMap = (Map<String, Object>) data.get("transactions");
            if (Objects.nonNull(transactionsMap)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> transactionArray = (List<Map<String, Object>>) transactionsMap.get("transaction");
                if (Objects.nonNull(transactionArray) && !transactionArray.isEmpty()) {
                    logger.debug("Found transactions.transaction with {} items", transactionArray.size());
                    for (Map<String, Object> transactionMap : transactionArray) {
                        if (shouldIncludeTransaction(transactionMap, requestedTransactionType)) {
                            transactions.add(new HashMap<>(transactionMap)); // Preserve all fields
                        }
                    }
                }
            }
            
            // Check if data contains a direct list/array of transactions
            for (String key : data.keySet()) {
                Object value = data.get(key);
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> listValue = (List<Object>) value;
                    if (!listValue.isEmpty() && listValue.get(0) instanceof Map) {
                        logger.debug("Found list in key '{}' with {} items", key, listValue.size());
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> transactionArray = (List<Map<String, Object>>) (List<?>) listValue;
                        for (Map<String, Object> transactionMap : transactionArray) {
                            // Check if this looks like a transaction
                            if (transactionMap.containsKey("transactionDesc") || transactionMap.containsKey("transactionType")) {
                                if (shouldIncludeTransaction(transactionMap, requestedTransactionType)) {
                                    transactions.add(new HashMap<>(transactionMap)); // Preserve all fields
                                }
                            }
                        }
                    }
                }
            }
        }

        logger.info("Extracted {} transactions after filtering", transactions.size());
        return transactions;
    }
    
    /**
     * Checks if a transaction should be included based on the requested transactionType filter
     */
    private boolean shouldIncludeTransaction(Map<String, Object> transactionMap, String requestedTransactionType) {
        if (Objects.isNull(requestedTransactionType) || requestedTransactionType.trim().isEmpty()) {
            return true; // Include all if no filter specified
        }
        
        Object transactionType = transactionMap.get("transactionType");
        if (Objects.isNull(transactionType)) {
            // Try case-insensitive match
            for (Map.Entry<String, Object> entry : transactionMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("transactionType")) {
                    transactionType = entry.getValue();
                    break;
                }
            }
        }
        
        if (Objects.isNull(transactionType)) {
            return false; // No transactionType field, exclude if filter is specified
        }
        
        return requestedTransactionType.equalsIgnoreCase(transactionType.toString().trim());
    }

    private ApiResponse<Map<String, Object>> emptyWithStatus(ApiResponse<?> source) {
        ApiResponse<Map<String, Object>> out = new ApiResponse<>();
        if (Objects.nonNull(source) && Objects.nonNull(source.getStatus())) {
            out.setStatus(source.getStatus());
        } else {
            out.setStatus(new ApiResponse.Status(SUCCESS_CODE, SUCCESS_DESCRIPTION));
        }
        out.setData(new ArrayList<>());
        return out;
    }
}

