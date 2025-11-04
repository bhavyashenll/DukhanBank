package com.example.api.controller;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.service.AccountDetailService;
import com.example.api.service.ProcessUIRequest;
import com.example.api.dto.AccountDetailsResponse;
import com.example.api.infrastructure.AppConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequireDeviceInfo
public class AccountDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(AccountDetailsController.class);

    @Autowired
    private ProcessUIRequest processUIRequest;

    @Autowired
    private AccountDetailService accountDetailService;

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
            ApiResponse<AccountDetailsResponse> validationResponse = accountDetailService.validateRequest(baseServiceRequest);
            if (validationResponse != null) {
                logger.warn("Request validation failed for ACCOUNT.DETAIL");
                return ResponseEntity.badRequest().body(validationResponse);
            }
            
            ApiResponse<Map<String, Object>> response = processUIRequest.processEndToEndFlow("ACCOUNT.DETAIL", baseServiceRequest, null);
            ApiResponse<AccountDetailsResponse> transformed = accountDetailService.postProcessAccountDetails(response, lang, baseServiceRequest);
            return ResponseEntity.ok(transformed);
        } catch (Exception e) {
            logger.error("Error processing place request for service: ACCOUNT.DETAIL", e);
            return ResponseEntity.ok(ApiResponse.error());
        }
    }
}