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
        try {
            String responseXml;
            if (appProperties.getMock().isMockResponse()) {
                responseXml = loadMockXml(serviceName);
            } else {
                String xmlContent = convertJsonToXml(serviceName, jsonRequest);
                logger.info("Sending XML to bank MQ service: {}", xmlContent);
                responseXml = bankMQService.sendToMQ(xmlContent);
            }

            List<Map<String, Object>> jsonData = xmlParsingService.parseXmlResponse(responseXml);
            return jsonData.isEmpty() ? ApiResponse.noDataFound() : ApiResponse.success(jsonData);

        } catch (Exception e) {
            return ApiResponse.error();
        }
    }

    public String convertJsonToXml(String serviceName, Map<String, Object> request) throws Exception {
        XmlConversionRequest requestXml = new XmlConversionRequest(request);
        requestValidatorService.validateRequest(serviceName, requestXml);
        Map<String, Object> headers = headerService.getHeadersForService(serviceName);
        return xmlGeneratorService.generateXmlFromXsd(serviceName, requestXml, headers);
    }

    private String loadMockXml(String serviceName) throws IOException {
        String mockFileName = "MockResponses/" + serviceName + ".xml";
        ClassPathResource resource = new ClassPathResource(mockFileName);
        if (!resource.exists()) {
            throw new IOException("Mock XML not found: " + mockFileName);
        }
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}