package com.dukhan.MQ.Helpers.controller;

import com.dukhan.MQ.Helpers.dto.ApiResponse;
import com.dukhan.MQ.Helpers.service.MQServiceOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BankServiceController {

    private static final Logger logger = LoggerFactory.getLogger(RateController.class);

    private final MQServiceOrchestrator orchestrator;

    @Autowired
    public BankServiceController(MQServiceOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/place_request/{serviceName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeRequest(
            @PathVariable String serviceName,
            @RequestBody Map<String, Object> jsonRequest) {
    
        serviceName = (serviceName.equalsIgnoreCase("createLead")) ? "CREATE.LEAD" : null;
        logger.info("Received request for {}", serviceName);
    
        if (serviceName == null) {
            return ResponseEntity.ok(ApiResponse.noDataFound());
        }
    
        ApiResponse<Map<String, Object>> response = orchestrator.processEndToEndFlow(serviceName, jsonRequest);
        return ResponseEntity.ok(response);
    }    
}


