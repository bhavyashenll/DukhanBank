package com.example.api.account.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.service.ProcessUIRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Objects;

@Service
public class CustomerAccountValidationService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerAccountValidationService.class);

    @Autowired
    private ProcessUIRequest processUIRequest;

    public ApiResponse<?> validate(BaseServiceRequest baseServiceRequest) {
        try {
            logger.info("Starting customer account validation");

            Map<String, Object> requestInfo = baseServiceRequest.getRequestInfo();
            String accountNumber = Objects.toString(requestInfo.get("acctNumber"),
                    Objects.toString(requestInfo.get("accountNumber"), null));
            String customerId = Objects.toString(requestInfo.get("customerId"), null);

            logger.debug("Validating account: {} for customer: {}", accountNumber, customerId);

            requestInfo.put("filter", "ACCOUNTS");
            logger.debug("Added filter=ACCOUNTS to requestInfo");

            // Update customerId to customerNumber for SIGN.ON service call
            if (requestInfo.containsKey("customerId")) {
                requestInfo.put("customerNumber", customerId);
                requestInfo.remove("customerId");
                logger.debug("Replaced customerId with customerNumber in requestInfo");
            }

            ApiResponse<Map<String, Object>> signOnResponse = processUIRequest.processEndToEndFlow(
                    "SIGN.ON",
                    baseServiceRequest,
                    null);

            if (Objects.isNull(signOnResponse)) {
                logger.error("Received null response from SIGN.ON service");
                return ApiResponse.invalidData();
            }

            logger.info("SIGN.ON Response Status: {}",
                    Objects.nonNull(signOnResponse.getStatus()) ? signOnResponse.getStatus().getCode() : "null");

            if (Objects.nonNull(signOnResponse.getStatus())) {
                String statusCode = signOnResponse.getStatus().getCode();

                if (!"0000".equals(statusCode) && !"000000".equals(statusCode)) {
                    logger.warn("SIGN.ON service returned error status: {}", statusCode);
                    ApiResponse<?> errorResponse = new ApiResponse<>();
                    errorResponse.setStatus(new ApiResponse.Status(statusCode, signOnResponse.getStatus().getDescription()));
                    return errorResponse;
                }
            }

            Object responseData = signOnResponse.getData();

            if (Objects.isNull(responseData)) {
                logger.error("Response data is null from SIGN.ON service");
                return ApiResponse.invalidData();
            }

            logger.info("Response data type: {}", responseData.getClass().getName());

            boolean accountFound = false;

            if (responseData instanceof List) {
                @SuppressWarnings("unchecked")
                List<?> dataList = (List<?>) responseData;
                logger.info("Response is a List with {} items", dataList.size());

                for (Object item : dataList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        if (validateAccountInResponseMap(itemMap, accountNumber)) {
                            accountFound = true;
                            break;
                        }
                    }
                }
            } else if (responseData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) responseData;
                logger.info("Response is a Map with keys: {}", dataMap.keySet());
                accountFound = validateAccountInResponseMap(dataMap, accountNumber);
            }

            if (accountFound) {
                logger.info("Account validation successful for account: {}", accountNumber);
                ApiResponse<?> successResponse = new ApiResponse<>();
                successResponse.setStatus(new ApiResponse.Status("000000", null));
                return successResponse;
            } else {
                logger.warn("Account not found in customer's accounts: {}", accountNumber);
                return ApiResponse.invalidData();
            }

        } catch (Exception e) {
            logger.error("Error during account validation", e);
            return ApiResponse.invalidData();
        }
    }

    private boolean validateAccountInResponseMap(Map<String, Object> responseData, String accountNumber) {
        try {
            logger.debug("Validating account in response map. Keys: {}", responseData.keySet());

            Object accountsObj = null;

            if (responseData.containsKey("accounts")) {
                accountsObj = responseData.get("accounts");
                logger.debug("Found 'accounts' key directly");
            }

            else if (responseData.containsKey("acctNumber")) {
                logger.debug("Response map itself contains account data");
                return validateSingleAccount(responseData, accountNumber);
            }

            if (Objects.isNull(accountsObj)) {
                logger.warn("No 'accounts' key found in response. Available keys: {}", responseData.keySet());
                return false;
            }

            if (accountsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<?> accountsList = (List<?>) accountsObj;
                logger.info("Found {} accounts in response", accountsList.size());

                for (Object accountObj : accountsList) {
                    if (accountObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<?, ?> account = (Map<?, ?>) accountObj;
                        if (validateSingleAccount(account, accountNumber)) {
                            return true;
                        }
                    }
                }
            }

            else if (accountsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<?, ?> account = (Map<?, ?>) accountsObj;
                logger.info("Found single account in response");
                return validateSingleAccount(account, accountNumber);
            }

            logger.debug("Account number not found in response: {}", accountNumber);
            return false;

        } catch (Exception e) {
            logger.error("Error validating account in response", e);
            return false;
        }
    }

    private boolean validateSingleAccount(Map<?, ?> account, String accountNumber) {
        String acctNum = Objects.toString(account.get("acctNumber"), null);

        logger.debug("Comparing account: {} with requested: {}", acctNum, accountNumber);

        if (accountNumber.equals(acctNum)) {
            logger.info("Account number matched: {}", accountNumber);
            logger.debug("Account details: IBAN={}, Type={}, Status={}",
                    account.get("iban"),
                    account.get("acctType"),
                    account.get("acctStatus"));
            return true;
        }

        return false;
    }
}

