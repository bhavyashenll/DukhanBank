package com.example.api.account.controller;

import com.example.api.account.service.CustomerAccountValidationService;
import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.account.service.AccountTransactionService;
import com.example.api.service.ProcessUIRequest;
import com.example.api.infrastructure.AppConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@RequireDeviceInfo
public class AccountTransactionController {

    private static final Logger logger = LoggerFactory.getLogger(AccountTransactionController.class);

    @Autowired
    private ProcessUIRequest processUIRequest;

    @Autowired
    private AccountTransactionService accountTransactionService;

    @Autowired
    private CustomerAccountValidationService customerAccountValidationService;

    @PostMapping("/account-transactions")
    public ResponseEntity<ApiResponse<?>> getAccountTransactions(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, defaultValue = AppConstant.DEFAULT_LANGUAGE) String lang,
            @RequestHeader(name = AppConstant.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID) String screenName,
            @RequestHeader(name = AppConstant.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        try {
            logger.info("Processing account transaction request");

            ApiResponse<Map<String, Object>> validationResponse =
                    accountTransactionService.validateRequest(baseServiceRequest);

            if (Objects.nonNull(validationResponse)) {
                logger.warn("Request validation failed for TRANSACTION.STATEMENT");
                return ResponseEntity.badRequest().body(validationResponse);
            }

            ApiResponse<?> accountValidationResponse = customerAccountValidationService.validate(baseServiceRequest);

            if (Objects.nonNull(accountValidationResponse) && Objects.nonNull(accountValidationResponse.getStatus()) 
                    && !"000000".equals(accountValidationResponse.getStatus().getCode())) {
                logger.warn("Customer account validation failed with status code: {}", 
                        accountValidationResponse.getStatus().getCode());
                return ResponseEntity.badRequest().body(accountValidationResponse);
            }
            logger.info("Customer account validated successfully");

            ApiResponse<Map<String, Object>> response =
                    processUIRequest.processEndToEndFlow("TRANSACTION.STATEMENT", baseServiceRequest, null);
            
            ApiResponse<Map<String, Object>> transactionResponse =
                    accountTransactionService.postProcessAccountTransactions(response, baseServiceRequest);

            logger.info("Account transaction request processed successfully");
            return ResponseEntity.ok(transactionResponse);

        } catch (Exception e) {
            logger.error("Error processing request for TRANSACTION.STATEMENT", e);
            return ResponseEntity.ok(ApiResponse.error());
        }
    }
}

