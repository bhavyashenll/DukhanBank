package com.example.api.account.controller;

import com.example.api.account.service.CustomerAccountValidationService;
import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.account.service.DownloadEstatementService;
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
public class DownloadEstatementController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadEstatementController.class);

    @Autowired
    private DownloadEstatementService downloadEstatementService;

    @Autowired
    private CustomerAccountValidationService customerAccountValidationService;

    @PostMapping("/download-estatement")
    public ResponseEntity<ApiResponse<Map<String, Object>>> downloadEstatement(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, defaultValue = AppConstant.DEFAULT_LANGUAGE) String lang,
            @RequestHeader(name = AppConstant.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID) String screenName,
            @RequestHeader(name = AppConstant.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        try {

            ApiResponse<Map<String, Object>> validationResponse =
                    downloadEstatementService.validateRequest(baseServiceRequest);

            if (Objects.nonNull(validationResponse)
                    && !"000000".equals(validationResponse.getStatus().getCode())) {
                logger.warn("Validation failed for ESTATEMENT.REQUEST: {}",
                        validationResponse.getStatus().getDescription());
                return ResponseEntity.badRequest().body(validationResponse);
            }

            ApiResponse<?> accountValidationResponse = customerAccountValidationService.validate(baseServiceRequest);

            if (Objects.nonNull(accountValidationResponse) && Objects.nonNull(accountValidationResponse.getStatus()) 
                    && !"000000".equals(accountValidationResponse.getStatus().getCode())) {
                logger.warn("Customer account validation failed with status code: {}", 
                        accountValidationResponse.getStatus().getCode());
                @SuppressWarnings("unchecked")
                ApiResponse<Map<String, Object>> errorResponse = (ApiResponse<Map<String, Object>>) accountValidationResponse;
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> requestInfo = baseServiceRequest.getRequestInfo();

            ApiResponse<Map<String, Object>> downloadResponse =
                    downloadEstatementService.downloadEstatement(requestInfo);

            if (Objects.isNull(downloadResponse)) {
                logger.warn("No data found for download-estatement request.");
                return ResponseEntity.ok(ApiResponse.noDataFound());
            }

            // Check if the response indicates an error
            if (Objects.nonNull(downloadResponse.getStatus()) 
                    && !"000000".equals(downloadResponse.getStatus().getCode())) {
                logger.warn("downloadEstatement returned error status: {}", 
                        downloadResponse.getStatus().getCode());
                // Return appropriate HTTP status based on error code
                if ("000400".equals(downloadResponse.getStatus().getCode())) {
                    return ResponseEntity.badRequest().body(downloadResponse);
                } else if ("000404".equals(downloadResponse.getStatus().getCode())) {
                    return ResponseEntity.ok(downloadResponse); // 404 in body, but 200 OK HTTP
                } else {
                    return ResponseEntity.ok(downloadResponse);
                }
            }

            logger.info("Successfully processed download-estatement for customer.");
            return ResponseEntity.ok(downloadResponse);

        } catch (Exception e) {
            logger.error("Error while processing download-estatement request", e);
            return ResponseEntity.ok(ApiResponse.error());
        }
    }

}

