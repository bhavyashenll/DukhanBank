package com.example.api.infrastructure.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.api.dto.BaseServiceRequest;

/**
 * Helper class for preparing MQ service requests
 */
@Component
public class MQRequestHelper {

    private static final Logger logger = LoggerFactory.getLogger(MQRequestHelper.class);

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ServiceDefaultFieldsHelper serviceDefaultFieldsHelper;


    /**
     * Prepares MQ request with enhanced field processing:
     * 1. Split NameofPerson (earlier FullName) into FirstName and LastName at first space
     * 2. All fields use same fieldKey (fieldName)
     * 3. All fields are concatenated as description without XML tags
     * 4. Add serviceType field based on screenName
     * @param serviceName The service name
     * @param baseServiceRequest The original request
     * @param defaultFields Map of default field names and values to add
     * @param screenName The screen name to derive serviceType
     * @return JSON string representing the enhanced MQ request format
     */
    public String prepareMQRequest(String serviceName, 
                                                    BaseServiceRequest baseServiceRequest, 
                                                    Map<String, Object> defaultFields,
                                                    String screenName) {
        logger.info("Preparing enhanced MQ request as JSON for service: {}", serviceName);
        
        try {
            List<Map<String, Object>> parameters = new ArrayList<>();
            StringBuilder descriptionBuilder = new StringBuilder();
            
            // Add default fields first
            if (Objects.nonNull(defaultFields)) {
                defaultFields.forEach((fieldName, fieldValue) -> {
                    if (Objects.nonNull(fieldValue)) {
                        parameters.add(Map.of(
                            "fieldName", fieldName,
                            "fieldValue", fieldValue.toString()
                        ));
                    }
                });
            }
            
            // Add static fields based on screenName
            if (Objects.nonNull(screenName)) {
                Map<String, Object> staticFields = serviceDefaultFieldsHelper.deriveStaticFieldsFromScreenName(screenName);
                staticFields.forEach((fieldName, fieldValue) -> {
                    if (Objects.nonNull(fieldValue)) {
                        parameters.add(Map.of(
                            "fieldName", fieldName,
                            "fieldValue", fieldValue.toString()
                        ));
                    }
                });
                logger.info("Added static fields: {} for screenName: {}", staticFields, screenName);
            }
            
            // Process request fields - handle null or empty requestInfo
            if (Objects.nonNull(baseServiceRequest.getRequestInfo()) && !baseServiceRequest.getRequestInfo().isEmpty()) {
                baseServiceRequest.getRequestInfo().forEach((fieldName, fieldValue) -> {
                    if (Objects.nonNull(fieldValue)) {
                        String stringValue = Objects.toString(fieldValue, "");
                        
                        // 1. Handle NameofPerson (earlier FullName) splitting into FirstName and LastName
                        if ("NameofPerson".equalsIgnoreCase(fieldName) && !stringValue.trim().isEmpty()) {
                            String[] nameParts = stringValue.trim().split("\\s+", 2);
                            if (nameParts.length >= 1) {
                                parameters.add(Map.of(
                                    "fieldName", "firstName",
                                    "fieldValue", nameParts[0]
                                ));
                            }
                            if (nameParts.length >= 2) {
                                parameters.add(Map.of(
                                    "fieldName", "lastName", 
                                    "fieldValue", nameParts[1]
                                ));
                            }
                        }
                        
                        // 2. Add all fields as individual parameters
                        parameters.add(Map.of(
                            "fieldName", fieldName,
                            "fieldValue", stringValue
                        ));
                        
                        // 3. All fields also go to description (without XML tags)
                        if (descriptionBuilder.length() > 0) {
                            descriptionBuilder.append(" ");
                        }
                        descriptionBuilder.append(fieldName).append(":").append(stringValue);
                    }
                });
            } else {
                logger.warn("RequestInfo is null or empty for service: {}", serviceName);
            }
            
            // 3. Add description field without XML tags
            if (descriptionBuilder.length() > 0) {
                parameters.add(Map.of(
                    "fieldName", "description",
                    "fieldValue", descriptionBuilder.toString()
                ));
            }

            //TODO - remove after middleware service is corrected
            String updatedServiceName = (serviceName.equalsIgnoreCase("EXCHANGE.RATES") ? "EXCHANGE.RATE" : serviceName);
            Map<String, Object> mqRequest = Map.of(
                "serviceName", updatedServiceName,
                "parameters", parameters
            );
            
            String jsonRequest = objectMapper.writeValueAsString(mqRequest);
            logger.info("Prepared enhanced MQ request JSON for service: {} - JSON: {}", serviceName, jsonRequest);
            return jsonRequest;
        } catch (JsonProcessingException e) {
            logger.error("Error converting enhanced MQ request to JSON for service: {}", serviceName, e);
            return "{}";
        }
    }
}

