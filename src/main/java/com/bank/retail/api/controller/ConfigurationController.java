package com.bank.retail.api.controller;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.ConfigurationDto;
import com.bank.retail.api.dto.RequestHeadersDto;
import com.bank.retail.engine.service.ConfigurationService;
import com.bank.retail.exception.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/configurations")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/callback/{screenId}")
    public ResponseEntity<ApiResponse<ConfigurationDto>> getCallbackFields(
            @PathVariable Long screenId,
            @RequestHeader(name = "serviceId", required = true) String serviceId,
            @RequestHeader(name = "channel", required = true) String channel,
            @RequestHeader(name = "accept-language", required = true) String lang,
            @RequestHeader(name = "unit", required = true) String unit,
            @RequestHeader(name = "ipAddress", required = true) String ipAddress,
            @RequestHeader(name = "deviceId", required = true) String deviceId,
            @RequestHeader(name = "username", required = true) String username,
            @RequestHeader(name = "userId", required = true) String userId,
            @RequestHeader(name = "partnerId", required = true) String partnerId,
            @RequestHeader(name = "x-correlationId", required = true) String correlationId,
            @RequestHeader(name = "authType", required = true) String authType) {
        
        validateHeaders(serviceId, channel, lang, unit, ipAddress, deviceId, 
                      username, userId, partnerId, correlationId, authType);
        
        RequestHeadersDto headers = new RequestHeadersDto(
                serviceId, channel, lang, unit, ipAddress, deviceId, username, userId, partnerId, correlationId, authType);
        
        ApiResponse<ConfigurationDto> response = configurationService.getRequestCallbackFields(screenId, headers);
        return ResponseEntity.ok(response);
    }
    
    private void validateHeaders(String serviceId, String channel, String lang, String unit,
                               String ipAddress, String deviceId, String username, String userId,
                               String partnerId, String correlationId, String authType) {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new ValidationException("serviceId header is required");
        }
        if (channel == null || channel.trim().isEmpty()) {
            throw new ValidationException("channel header is required");
        }
        if (lang == null || lang.trim().isEmpty()) {
            throw new ValidationException("accept-language header is required");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new ValidationException("unit header is required");
        }
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new ValidationException("ipAddress header is required");
        }
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new ValidationException("deviceId header is required");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("username header is required");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new ValidationException("userId header is required");
        }
        if (partnerId == null || partnerId.trim().isEmpty()) {
            throw new ValidationException("partnerId header is required");
        }
        if (correlationId == null || correlationId.trim().isEmpty()) {
            throw new ValidationException("x-correlationId header is required");
        }
        if (authType == null || authType.trim().isEmpty()) {
            throw new ValidationException("authType header is required");
        }
    }
}
