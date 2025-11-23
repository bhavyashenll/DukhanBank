package com.example.api.account.service.impl;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.account.service.DownloadEstatementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DownloadEstatementServiceImpl implements DownloadEstatementService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadEstatementServiceImpl.class);

    @Autowired
    private com.example.api.repository.OciBarwaStatementRepository repository;

    @Override
    public ApiResponse<Map<String, Object>> validateRequest(BaseServiceRequest baseServiceRequest) {
        logger.info("Validating eStatement request");

        if (Objects.isNull(baseServiceRequest) || Objects.isNull(baseServiceRequest.getRequestInfo())) {
            logger.warn("Request or requestInfo is null");
            ApiResponse<Map<String, Object>> response = new ApiResponse<>();
            response.setStatus(new ApiResponse.Status("000400", "Bad request"));
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("valid", false);
            errorData.put("message", "Request validation failed");
            errorData.put("errors", List.of("Request or requestInfo is null"));
            response.setData(List.of(errorData));
            return response;
        }

        Map<String, Object> requestInfo = baseServiceRequest.getRequestInfo();
        List<String> errors = new ArrayList<>();

        // Validate accountNumber
        Object accountNumber = requestInfo.get("accountNumber");
        if (Objects.isNull(accountNumber) || (accountNumber.toString().trim().isEmpty())) {
            errors.add("accountNumber is required");
        }

        // Validate statementType
        Object statementType = requestInfo.get("statementType");
        if (Objects.isNull(statementType) || (statementType.toString().trim().isEmpty())) {
            errors.add("statementType is required");
        }

        // Validate customerId
        Object customerId = requestInfo.get("customerId");
        if (Objects.isNull(customerId) || (customerId.toString().trim().isEmpty())) {
            errors.add("customerId is required");
        }

        // Validate date
        Object date = requestInfo.get("date");
        if (Objects.isNull(date) || (date.toString().trim().isEmpty())) {
            errors.add("date is required");
        } else {
            // Validate date format MM/yyyy
            String dateStr = date.toString().trim();
            try {
                YearMonth.parse(dateStr, DateTimeFormatter.ofPattern("MM/yyyy"));
            } catch (Exception e) {
                errors.add("date must be in MM/yyyy format");
                logger.warn("Invalid date format: {}", dateStr);
            }
        }

        if (!errors.isEmpty()) {
            logger.warn("Validation failed: {}", errors);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>();
            response.setStatus(new ApiResponse.Status("000400", "Bad request"));
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("valid", false);
            errorData.put("message", "Request validation failed");
            errorData.put("errors", errors);
            response.setData(List.of(errorData));
            return response;
        }

        logger.info("Request validation successful");
        return null;
    }

    @Override
    public ApiResponse<Map<String, Object>> downloadEstatement(Map<String, Object> request) {
        try {
            logger.info("downloadEstatement called with request keys: {}", request != null ? request.keySet() : "null");
            
            // Extract fields with null-safe conversion to String
            // Note: CustomerAccountValidationService replaces customerId with customerNumber, so check both
            String customerId = null;
            if (request.get("customerId") != null) {
                customerId = String.valueOf(request.get("customerId"));
            } else if (request.get("customerNumber") != null) {
                customerId = String.valueOf(request.get("customerNumber"));
            }
            
            String accountNumber = request.get("accountNumber") != null ? String.valueOf(request.get("accountNumber")) : null;
            String statementType = request.get("statementType") != null ? String.valueOf(request.get("statementType")) : null;
            String dateStr = request.get("date") != null ? String.valueOf(request.get("date")) : null;

            logger.info("Extracted fields - customerId: {}, accountNumber: {}, statementType: {}, date: {}", 
                    customerId, accountNumber, statementType, dateStr);

            // Check for null or empty strings
            if (Objects.isNull(customerId) || customerId.trim().isEmpty() ||
                Objects.isNull(accountNumber) || accountNumber.trim().isEmpty() ||
                Objects.isNull(statementType) || statementType.trim().isEmpty() ||
                Objects.isNull(dateStr) || dateStr.trim().isEmpty()) {
                logger.warn("Missing required fields - customerId: {}, accountNumber: {}, statementType: {}, date: {}", 
                        customerId != null && !customerId.trim().isEmpty(), 
                        accountNumber != null && !accountNumber.trim().isEmpty(), 
                        statementType != null && !statementType.trim().isEmpty(), 
                        dateStr != null && !dateStr.trim().isEmpty());
                return ApiResponse.badRequest();
            }

            YearMonth yearMonth = YearMonth.parse(dateStr, DateTimeFormatter.ofPattern("MM/yyyy"));
            int month = yearMonth.getMonthValue();
            int year = yearMonth.getYear();

            Optional<com.example.api.entity.OciBarwaStatement> stmtOpt = repository.findStatement(
                    customerId, accountNumber, statementType, month, year
            );

            if (stmtOpt.isPresent()) {
                logger.info("Found statement for customer: {}, account: {}, type: {}, month: {}, year: {}",
                        customerId, accountNumber, statementType, month, year);
                return ApiResponse.success(
                        List.of(Map.of("path", stmtOpt.get().getPath()))
                );
            } else {
                logger.warn("No statement found for customer: {}, account: {}, type: {}, month: {}, year: {}",
                        customerId, accountNumber, statementType, month, year);
                return ApiResponse.noDataFound();
            }

        } catch (Exception e) {
            logger.error("Error in downloadEstatement", e);
            return ApiResponse.error();
        }
    }
}

