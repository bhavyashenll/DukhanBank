package com.example.api.controller;


import com.example.api.dto.ApiResponse;
import com.example.api.dto.UsernameSuggestionRequest;
import com.example.api.dto.UsernameSuggestionResponse;
import com.example.api.infrastructure.AppConstant;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.infrastructure.ResponseConstant;
import com.example.api.service.UsernameSuggestionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/username")
@Slf4j
@RequireDeviceInfo
public class UserNameSuggestionController {

    private final UsernameSuggestionService usernameService;

    public UserNameSuggestionController(UsernameSuggestionService usernameService) {
        this.usernameService = usernameService;
    }

    @PostMapping("/suggest")
    public ResponseEntity<Map<String, Object>> suggestUsernames(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL, required = true) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, required = false) String lang,
            @RequestHeader(name = AppConstant.SERVICEID, required = true) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID, required = true) String screenId,
            @RequestHeader(name = AppConstant.MODULE_ID, required = true) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID, required = true) String subModuleId,
            @Valid @RequestBody UsernameSuggestionRequest request) {

        String customerId = request.getRequestInfo() != null ? request.getRequestInfo().getCustomerId() : null;
        log.info("Received username suggestion request for customerId: {}", customerId);

        try {
            ApiResponse<UsernameSuggestionResponse> response = usernameService.generateUsernames(request);

            if (response.getStatus() != null &&
                    ResponseConstant.SUCCESS_CODE.equals(response.getStatus().getCode())) {
                response.getStatus().setDescription(ResponseConstant.process_msg);
            }

            log.info("Returning {} username suggestions for customerId: {}",
                    response.getData() != null ? response.getData().size() : 0, customerId);

            // Create custom response format with single object instead of array
            Map<String, Object> customResponse = new HashMap<>();
            customResponse.put("status", response.getStatus());
            
            // Extract the single object from the list
            if (response.getData() != null && !response.getData().isEmpty()) {
                customResponse.put("data", response.getData().get(0));
            } else {
                // Return empty array for error responses to match expected format
                customResponse.put("data", new ArrayList<>());
            }

            return ResponseEntity.ok(customResponse);
        } catch (Exception e) {
            log.error("Error processing username suggestion request for customerId: {}", customerId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", new ApiResponse.Status("000500", "Internal server error"));
            errorResponse.put("data", new ArrayList<>());
            return ResponseEntity.ok(errorResponse);
        }
    }
}


