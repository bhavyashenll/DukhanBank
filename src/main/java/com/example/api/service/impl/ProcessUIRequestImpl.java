package com.example.api.service.impl;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.infrastructure.config.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import com.example.api.service.ProcessUIRequest;
import com.example.api.service.ProcessResponseService;
import com.example.api.service.DeviceBehaviorService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component
public class ProcessUIRequestImpl implements ProcessUIRequest{

    private static final Logger logger = LoggerFactory.getLogger(ProcessUIRequest.class);    

    @Autowired
    private AppProperties appProperties;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProcessResponseService processResponseService;
    
    @Autowired
    private DeviceBehaviorService deviceBehaviorService;

    @Override
    public ApiResponse<Map<String, Object>> processEndToEndFlow(String serviceName, BaseServiceRequest baseServiceRequest, String screenName) {
        logger.info("Processing end-to-end flow for service: {}", serviceName);
        
        try {
            // Check device behavior using common service
            ApiResponse<Map<String, Object>> deviceResponse = deviceBehaviorService.checkDeviceBehavior(baseServiceRequest);
            if (deviceResponse != null) {
                return deviceResponse;
            }
            
            if (appProperties.getMock().isMockResponse()) {
                logger.debug("Using mock response for service: {}", serviceName);
                Map<String, Object> mockResponse = loadMockJson(serviceName);
                logger.info("Mock response: {}", mockResponse.toString());
                return processResponseService.transformBankResponse(mockResponse, serviceName);
            } else {
                // TODO - call MQ service and get bankResponse
                logger.debug("Calling MQ service for: {}", serviceName);
                // For now, return no data found
                return ApiResponse.noDataFound();
            }

        } catch (Exception e) {
            logger.error("Error processing end-to-end flow for service: {}", serviceName, e);
            return ApiResponse.error();
        }
    }

    private Map<String, Object> loadMockJson(String serviceName) throws IOException {
        logger.debug("Loading mock JSON for service: {}", serviceName);
        
        try {
            String mockFileName = "MockResponses/" + serviceName + ".json";
            ClassPathResource resource = new ClassPathResource(mockFileName);
            if (!resource.exists()) {
                logger.error("Mock JSON not found: {}", mockFileName);
                throw new IOException("Mock JSON not found: " + mockFileName);
            }
            
            // Read the JSON file content
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            
            // Parse the JSON to get the full response
            Map<String, Object> jsonResponse = objectMapper.readValue(content, new TypeReference<Map<String, Object>>() {});
            
            // Extract the bankResponse from the response
            @SuppressWarnings("unchecked")
            Map<String, Object> bankResponse = (Map<String, Object>) jsonResponse.get("bankResponse");
            
            if (bankResponse == null) {
                logger.warn("No 'bankResponse' field found in mock JSON for service: {}", serviceName);
                return new java.util.HashMap<>();
            }
            
            logger.debug("Successfully loaded mock JSON for service: {} with bankResponse", serviceName);
            return bankResponse;
        } catch (Exception e) {
            logger.error("Error loading mock JSON for service: {}", serviceName, e);
            throw new IOException("Failed to load mock JSON for service: " + serviceName, e);
        }
    }

    @Override
    public BaseServiceRequest createRequestWithPathParam(BaseServiceRequest originalRequest, String paramName, String paramValue) {
        Map<String, Object> requestData = originalRequest.getRequestData();
        Map<String, Object> updatedData = Objects.nonNull(requestData) ? new java.util.HashMap<>(requestData) : new java.util.HashMap<>();
        
        // Only add path parameter if both paramName and paramValue are provided and valid
        if (Objects.nonNull(paramName) && Objects.nonNull(paramValue) && !paramValue.isBlank()) {
            updatedData.put(paramName, paramValue);
        }
        
        return new BaseServiceRequest(updatedData, originalRequest.getDeviceInfo());
    }
}
