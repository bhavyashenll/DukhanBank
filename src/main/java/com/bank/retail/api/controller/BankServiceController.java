package com.bank.retail.api.controller;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.engine.service.MQServiceOrchestrator;
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

    private final MQServiceOrchestrator orchestrator;

    @Autowired
    public BankServiceController(MQServiceOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/place_request/{serviceName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeRequest(
            @PathVariable String serviceName,
            @RequestBody Map<String, Object> jsonRequest) {

        serviceName = (serviceName.equalsIgnoreCase("createLead")) ? "CREATE.LEAD" : serviceName;
        ApiResponse<Map<String, Object>> response = orchestrator.processEndToEndFlow(serviceName, jsonRequest);
        return ResponseEntity.ok(response);
    }
}