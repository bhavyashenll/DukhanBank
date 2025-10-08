package com.bank.retail.engine.service.impl;

import com.bank.retail.api.dto.XmlConversionRequest;
import com.bank.retail.engine.service.RequestDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RequestDataServiceImpl implements RequestDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestDataServiceImpl.class);
    
    public Map<String, Object> prepareRequestData(XmlConversionRequest request, String serviceName) {
        logger.debug("Preparing request data for service: {}", serviceName);
        
        try {
            Map<String, Object> requestData = new java.util.LinkedHashMap<>(request);
            
            if (!requestData.containsKey("referenceNum")) {
                String referenceNum = String.valueOf(System.currentTimeMillis());
                requestData.put("referenceNum", referenceNum);
                logger.debug("Generated reference number: {}", referenceNum);
            }
            
            if (!requestData.containsKey("requestTime")) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                String requestTime = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                requestData.put("requestTime", requestTime);
                logger.debug("Generated request time: {}", requestTime);
            }
            
            // Set indexRate to 11 for EXCHANGE.RATE service if not provided in request
            if ("EXCHANGE.RATE".equals(serviceName) && !requestData.containsKey("indexRate")) {
                requestData.put("indexRate", 11);
                logger.debug("Set default indexRate to 11 for EXCHANGE.RATE service");
            }
            
            logger.debug("Successfully prepared request data with {} fields", requestData.size());
            return requestData;
        } catch (Exception e) {
            logger.error("Error preparing request data for service: {}", serviceName, e);
            throw new RuntimeException("Failed to prepare request data for service: " + serviceName, e);
        }
    }
}
