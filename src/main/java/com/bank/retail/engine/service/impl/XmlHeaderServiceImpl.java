package com.bank.retail.engine.service.impl;

import com.bank.retail.engine.service.XmlHeaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class XmlHeaderServiceImpl implements XmlHeaderService {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlHeaderService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> getHeadersForService(String serviceName) throws Exception {
        logger.debug("Fetching headers for service: {}", serviceName);
        
        try {
            String fileName = serviceName + ".json";
            ClassPathResource resource = new ClassPathResource("requestHeaders/" + fileName);
            
            if (!resource.exists()) {
                logger.error("Header file not found for service: {}", serviceName);
                throw new Exception("Header file not found for service: " + serviceName);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> headerData = objectMapper.readValue(resource.getInputStream(), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> headers = (Map<String, Object>) headerData.get("headers");
            
            if (headers == null) {
                logger.error("No headers section found in file for service: {}", serviceName);
                throw new Exception("No headers section found in file for service: " + serviceName);
            }
            
            logger.debug("Successfully loaded headers for service: {}", serviceName);
            return headers;
            
        } catch (IOException e) {
            logger.error("Error reading header file for service: {}", serviceName, e);
            throw new Exception("Error reading header file for service: " + serviceName, e);
        }
    }
}
