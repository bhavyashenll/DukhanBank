package com.dukhan.MQ.Helpers.controller;

import com.dukhan.MQ.Helpers.dto.ApiResponse;
import com.dukhan.MQ.Helpers.dto.XmlConversionRequest;
import com.dukhan.MQ.Helpers.dto.XmlResponse;
import com.dukhan.MQ.Helpers.service.MQServiceOrchestrator;
import com.dukhan.MQ.Helpers.service.XmlParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/xml")
public class XmlController {

    private static final Logger logger = LoggerFactory.getLogger(XmlController.class);
    private final XmlParsingService xmlParsingService;
    private final MQServiceOrchestrator orchestrator;

    public XmlController(XmlParsingService xmlParsingService, MQServiceOrchestrator orchestrator) {
        this.xmlParsingService = xmlParsingService;
        this.orchestrator = orchestrator;
    }

    @PostMapping("/convertFromXsd/{serviceName}")
    public ResponseEntity<?> convertXsdToXml(
            @PathVariable String serviceName,
            @RequestBody XmlConversionRequest request) throws Exception {

        String xmlContent = orchestrator.convertJsonToXml(serviceName, request);
        XmlResponse xmlResponse = new XmlResponse(xmlContent);
        ApiResponse<XmlResponse> response = ApiResponse.success(List.of(xmlResponse));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<Map<String, Object>>> parseXmlResponse(
            @RequestBody String xmlResponse) {

        logger.info("Processing XML parse request");

        try {
            List<Map<String, Object>> parsedData = xmlParsingService.parseXmlResponse(xmlResponse);

            if (parsedData.isEmpty()) {
                logger.info("No data found in XML response");
                ApiResponse<Map<String, Object>> response = ApiResponse.noDataFound();
                return ResponseEntity.ok(response);
            } else {
                logger.info("Successfully parsed XML with {} data items", parsedData.size());
                ApiResponse<Map<String, Object>> response = ApiResponse.success(parsedData);
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            logger.error("Error parsing XML response", e);
            ApiResponse<Map<String, Object>> response = ApiResponse.error();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}