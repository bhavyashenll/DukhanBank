package com.bank.retail.engine.service;

import com.bank.retail.api.dto.ApiResponse;

import java.util.Map;

public interface MQServiceOrchestrator {
    ApiResponse<Map<String, Object>> processEndToEndFlow(String serviceName, Map<String, Object> jsonRequest);
    String convertJsonToXml(String serviceName, Map<String, Object> request) throws Exception;
}
