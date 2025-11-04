package com.example.api.controller;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.infrastructure.AppConstant;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.service.CustomerNicknameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@RequireDeviceInfo
public class CustomerNicknameController {

    @Autowired
    private CustomerNicknameService service;

    @PostMapping("/get-customer-nickname")
    public ResponseEntity<Map<String, Object>> getCustomerNickname(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL, required = true) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, required = false) String lang,
            @RequestHeader(name = AppConstant.SERVICEID, required = true) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID, required = true) String screenId,
            @RequestHeader(name = AppConstant.MODULE_ID, required = true) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID, required = true) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        log.info("Received get customer nickname request");

        ApiResponse<Object> response = service.getCustomerNickname(baseServiceRequest);

        log.info("Returning customer nickname");

        Map<String, Object> customResponse = new HashMap<>();
        customResponse.put("status", response.getStatus());
        customResponse.put("data", response.getData());

        return ResponseEntity.ok(customResponse);
    }

    @PostMapping("/upsert-customer-nickname")
    public ResponseEntity<Map<String, Object>> upsertNickname(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL, required = true) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, required = false) String lang,
            @RequestHeader(name = AppConstant.SERVICEID, required = true) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID, required = true) String screenId,
            @RequestHeader(name = AppConstant.MODULE_ID, required = true) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID, required = true) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        log.info("Received upsert customer nickname request for account number: {}", baseServiceRequest.getRequestInfo().get("accountNumber")   );

        ApiResponse<Object> response = service.upsertCustomerNickname(baseServiceRequest);

        log.info("Completed upsert customer nickname");

        Map<String, Object> customResponse = new HashMap<>();
        customResponse.put("status", response.getStatus());
        customResponse.put("data", response.getData());

        return ResponseEntity.ok(customResponse);
    }

    @PostMapping("/delete-customer-nickname")
    public ResponseEntity<Map<String, Object>> deleteNickname(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL, required = true) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, required = false) String lang,
            @RequestHeader(name = AppConstant.SERVICEID, required = true) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID, required = true) String screenId,
            @RequestHeader(name = AppConstant.MODULE_ID, required = true) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID, required = true) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        log.info("Received delete customer nickname request");

        ApiResponse<Object> response = service.deleteCustomerNickname(baseServiceRequest);

        log.info("Completed delete customer nickname");

        Map<String, Object> customResponse = new HashMap<>();
        customResponse.put("status", response.getStatus());
        customResponse.put("data", response.getData());

        return ResponseEntity.ok(customResponse);
    }
}
