package com.bank.retail.engine.service.impl;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.XmlConversionRequest;
import com.bank.retail.config.AppProperties;
import com.bank.retail.engine.service.MQServiceOrchestrator;
import com.bank.retail.engine.service.RequestValidatorService;
import com.bank.retail.engine.service.XmlGeneratorService;
import com.bank.retail.engine.service.XmlParsingService;
import com.bank.retail.engine.service.XmlHeaderService;
import com.bank.retail.engine.service.BankMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MQServiceOrchestratorImpl implements MQServiceOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(MQServiceOrchestrator.class);    

    private final RequestValidatorService requestValidatorService;
    private final XmlGeneratorService xmlGeneratorService;
    private final XmlParsingService xmlParsingService;
    private final XmlHeaderService headerService;
    private final BankMQService bankMQService;
    private final AppProperties appProperties;

    @Autowired
    public MQServiceOrchestratorImpl(XmlGeneratorService xmlGeneratorService,
                                  XmlParsingService xmlParsingService,
                                  XmlHeaderService headerService,
                                  BankMQService bankMQService,
                                  AppProperties appProperties,
                                  RequestValidatorService requestValidatorService) {
        this.xmlGeneratorService = xmlGeneratorService;
        this.xmlParsingService = xmlParsingService;
        this.headerService = headerService;
        this.bankMQService = bankMQService;
        this.appProperties = appProperties;
        this.requestValidatorService = requestValidatorService;
    }

    public ApiResponse<Map<String, Object>> processEndToEndFlow(String serviceName, Map<String, Object> jsonRequest) {
        logger.info("Processing end-to-end flow for service: {}", serviceName);
        
        try {
            String responseXml;
            if (appProperties.getMock().isMockResponse()) {
                logger.debug("Using mock response for service: {}", serviceName);
                responseXml = loadMockXml(serviceName);
            } else {
                logger.debug("Converting JSON to XML for service: {}", serviceName);
                String xmlContent = convertJsonToXml(serviceName, jsonRequest);
                logger.info("Sending XML to bank MQ service: {}", xmlContent);
                responseXml = bankMQService.sendToMQ(xmlContent);
            }

            logger.debug("Parsing XML response for service: {}", serviceName);
            List<Map<String, Object>> jsonData = xmlParsingService.parseXmlResponse(responseXml);
            
            if (jsonData.isEmpty()) {
                logger.info("No data found in response for service: {}", serviceName);
                return ApiResponse.noDataFound();
            } else {
                logger.info("Successfully processed {} data items for service: {}", jsonData.size(), serviceName);
                return ApiResponse.success(jsonData);
            }

        } catch (Exception e) {
            logger.error("Error processing end-to-end flow for service: {}", serviceName, e);
            return ApiResponse.error();
        }
    }

    public String convertJsonToXml(String serviceName, Map<String, Object> request) throws Exception {
        logger.debug("Converting JSON to XML for service: {}", serviceName);
        
        try {
            XmlConversionRequest requestXml = new XmlConversionRequest(request);
            logger.debug("Validating request for service: {}", serviceName);
            requestValidatorService.validateRequest(serviceName, requestXml);
            
            logger.debug("Getting headers for service: {}", serviceName);
            Map<String, Object> headers = headerService.getHeadersForService(serviceName);
            
            logger.debug("Generating XML from XSD for service: {}", serviceName);
            return xmlGeneratorService.generateXmlFromXsd(serviceName, requestXml, headers);
        } catch (Exception e) {
            logger.error("Error converting JSON to XML for service: {}", serviceName, e);
            throw new Exception("Failed to convert JSON to XML for service: " + serviceName, e);
        }
    }

    private String loadMockXml(String serviceName) throws IOException {
        logger.debug("Loading mock XML for service: {}", serviceName);
        
        try {
            String mockFileName = "MockResponses/" + serviceName + ".xml";
            ClassPathResource resource = new ClassPathResource(mockFileName);
            if (!resource.exists()) {
                logger.error("Mock XML not found: {}", mockFileName);
                throw new IOException("Mock XML not found: " + mockFileName);
            }
            
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            logger.debug("Successfully loaded mock XML for service: {}", serviceName);
            return content;
        } catch (Exception e) {
            logger.error("Error loading mock XML for service: {}", serviceName, e);
            throw new IOException("Failed to load mock XML for service: " + serviceName, e);
        }
    }
}