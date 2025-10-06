package com.dukhan.MQ.Helpers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(name = "app.mock.isMockResponse", havingValue = "true")
public class MockResponseService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockResponseService.class);
    private final ObjectMapper objectMapper;
    
    public MockResponseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public Object getMockResponse(String serviceName) throws IOException {
        String mockFileName = serviceName + ".json";
        String classpathPath = "MockResponses/" + mockFileName;
        
        logger.info("Loading mock JSON response from classpath: {}", classpathPath);
        
        ClassPathResource resource = new ClassPathResource(classpathPath);
        if (!resource.exists()) {
            logger.warn("Mock response file not found in classpath: {}", classpathPath);
            throw new IOException("Mock response file not found: " + classpathPath);
        }
        
        String jsonContent = resource.getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(jsonContent, Object.class);
    }
}
