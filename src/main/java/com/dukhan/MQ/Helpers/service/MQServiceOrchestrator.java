package com.dukhan.MQ.Helpers.service;

import com.dukhan.MQ.Helpers.dto.ApiResponse;
import com.dukhan.MQ.Helpers.dto.XmlConversionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import com.dukhan.MQ.Helpers.config.AppProperties;

import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

@Service
public class MQServiceOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(MQServiceOrchestrator.class);
    
    private final RequestValidatorService requestValidatorService;    
    private final XmlGeneratorService xmlGeneratorService;
    private final XmlParsingService xmlParsingService;
    private final XmlHeaderService headerService;
    private final DummyMQService dummyMQService;
    
    private final AppProperties appProperties;
    
    @Autowired
    public MQServiceOrchestrator(XmlGeneratorService xmlGeneratorService,
                               XmlParsingService xmlParsingService,
                               XmlHeaderService headerService,
                               DummyMQService dummyMQService,
                               AppProperties appProperties,
                               RequestValidatorService requestValidatorService) {
        this.xmlGeneratorService = xmlGeneratorService;
        this.xmlParsingService = xmlParsingService;
        this.headerService = headerService;
        this.dummyMQService = dummyMQService;
        this.appProperties = appProperties;
        this.requestValidatorService = requestValidatorService;
    }
    
    /**
     * End-to-end flow: JSON -> XSD to XML -> MQ -> XML to JSON
     * @param serviceName the service name
     * @param jsonRequest the JSON request data
     * @return API response with JSON data
     */
    public ApiResponse<Map<String, Object>> processEndToEndFlow(String serviceName, Map<String, Object> jsonRequest) {
        try {
            logger.info("Starting end-to-end flow for service: {}", serviceName);
            
            String responseXml;
            if (appProperties.getMock().isMockResponse()) {
                // Skip steps 1 and 2 and use mock XML from classpath for the service
                logger.info("Mock mode enabled, loading XML from classpath for service: {}", serviceName);
                responseXml = loadMockXml(serviceName);
            } else {
                // Step 1: Convert JSON to XML using XSD
                String xmlContent = convertJsonToXml(serviceName, jsonRequest);
                logger.info("Generated XML: {}", xmlContent);
                
                // Step 2: Send XML to dummy MQ and get XML response
                responseXml = dummyMQService.sendToMQ(xmlContent);
                logger.debug("MQ Response XML: {}", responseXml);
            }
            
            // Step 3: Convert XML response to JSON
            List<Map<String, Object>> jsonData = xmlParsingService.parseXmlResponse(responseXml);
            
            if (jsonData.isEmpty()) {
                logger.info("No data found in MQ response");
                return ApiResponse.noDataFound();
            }
            
            logger.info("Successfully processed end-to-end flow with {} data items", jsonData.size());
            return ApiResponse.success(jsonData);
            
        } catch (Exception e) {
            logger.error("Error in end-to-end flow processing", e);
            return ApiResponse.error();
        }
    }
    
    /**
     * Convert JSON request to XML using XSD schema
     * For EXCHANGE.RATE service, adds currencyCode and hardcoded indexRate=11
     */
    public String convertJsonToXml(String serviceName, Map<String, Object> request) throws Exception {
        logger.debug("Converting JSON to XML for service: {}", serviceName);

        // Create XmlConversionRequest from JSON
        XmlConversionRequest requestXml = new XmlConversionRequest(request);           

        // Inline former orchestrator steps: validate then generate
        requestValidatorService.validateRequest(serviceName, requestXml);        
        
        // Fetch headers for the service and perform actual conversion
        Map<String, Object> headers = headerService.getHeadersForService(serviceName);     
        
        return xmlGeneratorService.generateXmlFromXsd(serviceName, requestXml, headers);
    }
    
    private String loadMockXml(String serviceName) throws IOException {
        String mockFileName = "MockResponses/" + serviceName + ".xml";
        ClassPathResource resource = new ClassPathResource(mockFileName);
        if (!resource.exists()) {
            logger.warn("Mock XML file not found: {}", mockFileName);
            throw new IOException("Mock XML not found: " + mockFileName);
        }
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
    
}
