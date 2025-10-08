package com.bank.retail.api.controller;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.XmlConversionRequest;
import com.bank.retail.api.dto.XmlResponse;
import com.bank.retail.engine.service.MQServiceOrchestrator;
import com.bank.retail.engine.service.XmlParsingService;
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

    private final XmlParsingService xmlParsingService;
    private final MQServiceOrchestrator orchestrator;

    public XmlController(XmlParsingService xmlParsingService, MQServiceOrchestrator orchestrator) {
        this.xmlParsingService = xmlParsingService;
        this.orchestrator = orchestrator;
    }

    @PostMapping("/convertFromXsd/{serviceName}")
    public ResponseEntity<ApiResponse<XmlResponse>> convertXsdToXml(
            @PathVariable String serviceName,
            @RequestBody XmlConversionRequest request) {

        try {
            String xmlContent = orchestrator.convertJsonToXml(serviceName, request);
            XmlResponse xmlResponse = new XmlResponse(xmlContent);
            ApiResponse<XmlResponse> response = ApiResponse.success(List.of(xmlResponse));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<XmlResponse> response = ApiResponse.error();
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<Map<String, Object>>> parseXmlResponse(
            @RequestBody String xmlResponse) {

        try {
            List<Map<String, Object>> parsedData = xmlParsingService.parseXmlResponse(xmlResponse);
            ApiResponse<Map<String, Object>> response = parsedData.isEmpty() 
                ? ApiResponse.noDataFound() 
                : ApiResponse.success(parsedData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> response = ApiResponse.error();
            return ResponseEntity.ok(response);
        }
    }
}