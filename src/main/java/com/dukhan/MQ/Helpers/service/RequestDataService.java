package com.dukhan.MQ.Helpers.service;

import com.dukhan.MQ.Helpers.dto.XmlConversionRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RequestDataService {
    
    public Map<String, Object> prepareRequestData(XmlConversionRequest request, String serviceName) {
        Map<String, Object> requestData = new java.util.LinkedHashMap<>(request);
        
        if (!requestData.containsKey("referenceNum")) {
            requestData.put("referenceNum", String.valueOf(System.currentTimeMillis()));
        }
        
        if (!requestData.containsKey("requestTime")) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String requestTime = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            requestData.put("requestTime", requestTime);
        }
        
        // Set indexRate to 11 for EXCHANGE.RATE service if not provided in request
        if ("EXCHANGE.RATE".equals(serviceName) && !requestData.containsKey("indexRate")) {
            requestData.put("indexRate", 11);
        }
        
        return requestData;
    }
}
