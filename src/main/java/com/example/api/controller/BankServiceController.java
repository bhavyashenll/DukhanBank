package com.example.api.controller;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.infrastructure.AppConstant;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.infrastructure.ResponseConstant;
import com.example.api.service.DeviceBehaviorService;
import com.example.api.service.FieldValidationService;
import com.example.api.service.ProcessUIRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.api.infrastructure.helper.ServiceDefaultFieldsHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;


@RestController
@RequestMapping("/api")
@RequireDeviceInfo
public class BankServiceController {

    private static final Logger logger = LoggerFactory.getLogger(BankServiceController.class);

    @Autowired
    private ProcessUIRequest processUIRequest;
    
    @Autowired
    private FieldValidationService fieldValidationService;
    
    @Autowired
    private ServiceDefaultFieldsHelper serviceDefaultFieldsHelper;
    @Autowired
    private DeviceBehaviorService deviceBehaviorService;
    /**
     * Place request endpoint with field validation based on screen configuration
     * serviceName is derived from screenName header
     */
    @PostMapping("/place_request")
    public ResponseEntity<ApiResponse<Map<String, Object>>> serviceName(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, defaultValue = AppConstant.DEFAULT_LANGUAGE) String lang,
            @RequestHeader(name = AppConstant.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID) String screenName,
            @RequestHeader(name = AppConstant.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        // Derive serviceName from screenName
        String serviceName = serviceDefaultFieldsHelper.deriveServiceNameFromScreenName(screenName);
        logger.info("Processing place request for service: {} with screen: {}", serviceName, screenName);

        try {

            ApiResponse<Map<String, Object>> deviceResponse = deviceBehaviorService.checkDeviceBehavior(baseServiceRequest);
            if (deviceResponse != null) {
                logger.warn("Device behavior simulation triggered for device {}", baseServiceRequest.getDeviceInfo().getDeviceId());
                return ResponseEntity.ok(deviceResponse);
            }


            if (lang != null && !lang.isEmpty() && lang.equalsIgnoreCase("EN")) {
                logger.info("Validating request fields for English language");
                ApiResponse<Map<String, Object>> validationResponse = fieldValidationService.validateRequest(screenName,
                        baseServiceRequest.getRequestInfo());

                if (fieldValidationService.isValidationFailed(validationResponse)) {
                    logger.warn("Validation failed for service: {} with screen: {}", serviceName, screenName);
                    return ResponseEntity.badRequest().body(validationResponse);
                }
            }

            logger.info("Processing request for service: {}", serviceName);
            
            ApiResponse<Map<String, Object>> response = processUIRequest.processEndToEndFlow(serviceName, baseServiceRequest, screenName);

            if (response.getStatus() != null &&
                    ResponseConstant.SUCCESS_CODE.equals(response.getStatus().getCode())) {
                response.getStatus().setDescription(ResponseConstant.submit_msg);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing place request for service: {}", serviceName, e);
            return ResponseEntity.ok(ApiResponse.error());
        }
    }
}