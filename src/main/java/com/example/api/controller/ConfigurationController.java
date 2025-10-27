package com.example.api.controller;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.dto.ConfigurationDto;
import com.example.api.infrastructure.AppConstant;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.infrastructure.ResponseConstant;
import com.example.api.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequireDeviceInfo
public class ConfigurationController {

    @Autowired
    private ConfigurationService configurationService;

    @PostMapping("/screen_configuration")
    public ResponseEntity<ApiResponse<ConfigurationDto>> getCallbackFields(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, defaultValue = AppConstant.DEFAULT_LANGUAGE) String lang,
            @RequestHeader(name = AppConstant.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID) String screenName,
            @RequestHeader(name = AppConstant.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        ApiResponse<ConfigurationDto> response = configurationService.getRequestCallbackFields(screenName, lang);


        if (response.getStatus() != null &&
                ResponseConstant.SUCCESS_CODE.equals(response.getStatus().getCode())) {
            response.getStatus().setDescription(ResponseConstant.process_msg);
        }
        return ResponseEntity.ok(response);
    }
}
