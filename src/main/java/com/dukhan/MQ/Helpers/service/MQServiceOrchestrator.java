package com.dukhan.MQ.Helpers.service;

import com.dukhan.MQ.Helpers.dto.ApiResponse;
import com.dukhan.MQ.Helpers.dto.XmlConversionRequest;
import com.dukhan.MQ.Helpers.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class MQServiceOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(MQServiceOrchestrator.class);

    private final RequestValidatorService requestValidatorService;
    private final XmlGeneratorService xmlGeneratorService;
    private final XmlParsingService xmlParsingService;
    private final XmlHeaderService headerService;
    private final BankMQService bankMQService;
    private final AppProperties appProperties;

    @Autowired
    public MQServiceOrchestrator(XmlGeneratorService xmlGeneratorService,
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

    /**
     * End-to-end flow: JSON -> XSD to XML -> MQ -> XML to JSON.
     *
     * @param serviceName the service name
     * @param jsonRequest the JSON request data
     * @return API response with JSON data
     */
    public ApiResponse<Map<String, Object>> processEndToEndFlow(String serviceName, Map<String, Object> jsonRequest) {
        try {
            logger.info("Starting end-to-end flow for service: {}", serviceName);

            String responseXml;
            if (appProperties.getMock().isMockResponse()) {
                logger.info("Mock mode enabled, loading XML from classpath for service: {}", serviceName);
                responseXml = loadMockXml(serviceName);
            } else {
                String xmlContent = convertJsonToXml(serviceName, jsonRequest);
                logger.info("Generated XML: {}", xmlContent);
                responseXml = bankMQService.sendToMQ(xmlContent);
                logger.debug("MQ Response XML: {}", responseXml);
            }

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
     * Convert JSON request to XML using XSD schema.
     * For EXCHANGE.RATE service, adds currencyCode and hardcoded indexRate=11.
     */
    public String convertJsonToXml(String serviceName, Map<String, Object> request) throws Exception {
        logger.debug("Converting JSON to XML for service: {}", serviceName);

        XmlConversionRequest requestXml = new XmlConversionRequest(request);
        requestValidatorService.validateRequest(serviceName, requestXml);

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