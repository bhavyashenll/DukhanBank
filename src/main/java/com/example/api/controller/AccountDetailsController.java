package com.example.api.controller;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.service.AccountDetailService;
import com.example.api.service.ProcessUIRequest;
import com.example.api.dto.AccountDetailsResponse;
import com.example.api.infrastructure.AppConstant;
import com.example.api.account.service.CustomerAccountValidationService;
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
public class AccountDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(AccountDetailsController.class);

    @Autowired
    private ProcessUIRequest processUIRequest;

    @Autowired
    private AccountDetailService accountDetailService;

    @Autowired
    private CustomerAccountValidationService customerAccountValidationService;

    /**
     * Retrieves account details for the cutomer's account number
     * Processes the MQ response and transforms it into AccountDetailsResponse format
     * Includes account balance, IBAN, account holder name, and bank information
     */
    @PostMapping("/view-account-details")
    public ResponseEntity<ApiResponse<AccountDetailsResponse>> serviceName(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, defaultValue = AppConstant.DEFAULT_LANGUAGE) String lang,
            @RequestHeader(name = AppConstant.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID) String screenName,
            @RequestHeader(name = AppConstant.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        try {
            logger.info("Processing account details request for service: ACCOUNT.DETAIL");
            
            // Validate request
            ApiResponse<AccountDetailsResponse> validationResponse = accountDetailService.validateRequest(baseServiceRequest);
            if (Objects.nonNull(validationResponse)) {
                logger.warn("Request validation failed for ACCOUNT.DETAIL");
                return ResponseEntity.badRequest().body(validationResponse);
            }
            
            // Validate customer account (skip for INTERNAL module)
            boolean isInternalModule = "INTERNAL".equalsIgnoreCase(moduleId);
            if (!isInternalModule) {
                ApiResponse<?> accountValidationResponse = customerAccountValidationService.validate(baseServiceRequest);
                if (Objects.nonNull(accountValidationResponse) && Objects.nonNull(accountValidationResponse.getStatus())
                        && !"000000".equals(accountValidationResponse.getStatus().getCode())) {
                    logger.warn("Customer account validation failed with status code: {}",
                            accountValidationResponse.getStatus().getCode());
                    @SuppressWarnings("unchecked")
                    ApiResponse<AccountDetailsResponse> errorResponse = (ApiResponse<AccountDetailsResponse>) accountValidationResponse;
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                logger.info("Customer account validated successfully");
            } else {
                logger.info("Skipped customer account validation for INTERNAL module");
            }
            
            // Process end-to-end flow
            ApiResponse<Map<String, Object>> response = processUIRequest.processEndToEndFlow("ACCOUNT.DETAIL", baseServiceRequest, null);
            
            if (Objects.isNull(response)) {
                logger.error("Received null response from processEndToEndFlow for ACCOUNT.DETAIL");
                return ResponseEntity.ok(ApiResponse.error());
            }
            
            logger.info("Received response from processEndToEndFlow with status: {}", 
                    response.getStatus() != null ? response.getStatus().getCode() : "null");
            
            // Transform response
            ApiResponse<AccountDetailsResponse> transformed = accountDetailService.postProcessAccountDetails(response, lang, baseServiceRequest);
            
            if (Objects.isNull(transformed)) {
                logger.error("Received null transformed response from postProcessAccountDetails");
                return ResponseEntity.ok(ApiResponse.error());
            }
            
            logger.info("Successfully processed account details request");
            return ResponseEntity.ok(transformed);
            
        } catch (Exception e) {
            logger.error("Error processing account details request for service: ACCOUNT.DETAIL", e);
            return ResponseEntity.ok(ApiResponse.error());
        }
    }
}