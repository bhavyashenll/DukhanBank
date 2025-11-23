package com.example.api.account.service.impl;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.account.service.DownloadTransactionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DownloadTransactionsServiceImpl implements DownloadTransactionsService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadTransactionsServiceImpl.class);

    @Override
    public ApiResponse<Map<String, Object>> validateRequest(BaseServiceRequest baseServiceRequest) {
        logger.info("Validating download transactions request");

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

        // Validate accountNumber or acctNumber
        Object accountNumber = requestInfo.get("accountNumber");
        if (Objects.isNull(accountNumber) || (accountNumber.toString().trim().isEmpty())) {
            Object acctNumber = requestInfo.get("acctNumber");
            if (Objects.isNull(acctNumber) || (acctNumber.toString().trim().isEmpty())) {
                errors.add("accountNumber or acctNumber is required");
            }
        }

        // Validate customerId
        Object customerId = requestInfo.get("customerId");
        if (Objects.isNull(customerId) || (customerId.toString().trim().isEmpty())) {
            errors.add("customerId is required");
        }

        // Validate segmentName
        Object segmentName = requestInfo.get("segmentName");
        if (Objects.nonNull(segmentName) && !segmentName.toString().trim().isEmpty()) {
            String segmentNameStr = segmentName.toString().trim();
            if (!segmentNameStr.equalsIgnoreCase("RETAIL") && !segmentNameStr.equalsIgnoreCase("PRIVATE")) {
                errors.add("Invalid segmentName. Must be 'Retail' or 'Private'");
            }
        }
        // Note: segmentName is optional and defaults to "retail" if not provided

        // Validate fieldType
        Object fieldType = requestInfo.get("fieldType");
        if (Objects.isNull(fieldType) || (fieldType.toString().trim().isEmpty())) {
            errors.add("fieldType is required");
        } else {
            String fieldTypeStr = fieldType.toString().trim();
            if (!fieldTypeStr.equalsIgnoreCase("pdf") && !fieldTypeStr.equalsIgnoreCase("xls")) {
                errors.add("Invalid fieldType. Must be 'pdf' or 'xls'");
            }
        }

        if (!errors.isEmpty()) {
            logger.warn("Validation failed: {}", errors);
            Map<String, Object> result = new HashMap<>();
            result.put("valid", false);
            result.put("message", "Request validation failed");
            result.put("errors", errors);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>();
            response.setStatus(new ApiResponse.Status("000400", "Bad request"));
            response.setData(List.of(result));
            return response;
        }

        logger.info("Request validation successful");
        return null;
    }
}

