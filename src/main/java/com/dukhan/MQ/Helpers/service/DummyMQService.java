package com.dukhan.MQ.Helpers.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class DummyMQService {
    
    private static final Logger logger = LoggerFactory.getLogger(DummyMQService.class);
    
    private final Random random = new Random();
    
    /**
     * Simulates sending XML to MQ and receiving XML response
     * This is a dummy implementation that returns a mock XML response
     * 
     * @param xmlInput the XML to send to MQ
     * @return mock XML response
     */
    public String sendToMQ(String xmlInput) {
        logger.info("Sending XML to dummy MQ service");
        logger.debug("Input XML: {}", xmlInput);
        
        try {
            // Simulate MQ processing delay
            Thread.sleep(100 + random.nextInt(200)); // 100-300ms delay
            
            // Generate mock XML response based on input
            String mockResponse = generateMockMQResponse(xmlInput);
            
            logger.info("Received response from dummy MQ service");
            logger.debug("Response XML: {}", mockResponse);
            
            return mockResponse;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("MQ processing interrupted", e);
            throw new RuntimeException("MQ processing interrupted", e);
        } catch (Exception e) {
            logger.error("Error in dummy MQ processing", e);
            throw new RuntimeException("MQ processing failed", e);
        }
    }
    
    /**
     * Generates a mock XML response based on the input XML
     * This simulates what would come back from a real MQ service
     */
    private String generateMockMQResponse(String xmlInput) {
        // Simple mock response - in a real implementation, this would be more sophisticated
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<NS1:eAI_MESSAGE xmlns:NS1=\"urn:esbbank.com/gbo/xml/schemas/v1_0/\">" +
               "<NS1:eAI_HEADER>" +
               "<NS1:serviceName>MOCK_SERVICE</NS1:serviceName>" +
               "<NS1:returnCode>000000</NS1:returnCode>" +
               "</NS1:eAI_HEADER>" +
               "<NS1:eAI_BODY>" +
               "<NS1:eAI_REPLY>" +
               "<NS1:mockData>" +
               "<NS1:result>SUCCESS</NS1:result>" +
               "<NS1:timestamp>" + System.currentTimeMillis() + "</NS1:timestamp>" +
               "<NS1:processedBy>DummyMQService</NS1:processedBy>" +
               "</NS1:mockData>" +
               "</NS1:eAI_REPLY>" +
               "</NS1:eAI_BODY>" +
               "</NS1:eAI_MESSAGE>";
    }
}
