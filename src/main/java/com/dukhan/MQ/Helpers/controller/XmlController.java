package com.dukhan.MQ.Helpers.controller;

import com.dukhan.MQ.Helpers.dto.ApiResponse;
import com.dukhan.MQ.Helpers.dto.XmlConversionRequest;
import com.dukhan.MQ.Helpers.dto.XmlResponse;
import com.dukhan.MQ.Helpers.service.MQServiceOrchestrator;
import com.dukhan.MQ.Helpers.service.XmlHeaderService;
import com.dukhan.MQ.Helpers.service.RequestValidatorService;
import com.dukhan.MQ.Helpers.service.XmlGeneratorService;
import com.dukhan.MQ.Helpers.service.XmlParsingService;
import com.dukhan.MQ.Helpers.util.LanguageMappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/xml")
public class XmlController {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlController.class);
    
    private final RequestValidatorService requestValidatorService;
    private final XmlGeneratorService xmlGeneratorService;
    private final XmlParsingService xmlParsingService;
    private final XmlHeaderService headerService;
    
    public XmlController(RequestValidatorService requestValidatorService,
                        XmlGeneratorService xmlGeneratorService,
                        XmlParsingService xmlParsingService,
                        XmlHeaderService headerService,
                        MQServiceOrchestrator endToEndFlowService) {
        this.requestValidatorService = requestValidatorService;
        this.xmlGeneratorService = xmlGeneratorService;
        this.xmlParsingService = xmlParsingService;
        this.headerService = headerService;
    }
    
    @PostMapping("/convertFromXsd")
    public ResponseEntity<?> convertXsdToXml(
            @PathVariable String serviceName,
            @RequestBody XmlConversionRequest request,
            @RequestHeader(value = "language", required = false) String language) throws Exception {
        
        logger.info("Processing convert request for service: {}", serviceName);
        
        // Fetch headers for the service and perform actual conversion
        Map<String, Object> headers = headerService.getHeadersForService(serviceName);
        
        // Set requestorLanguage based on language header if provided
        if (language != null && !language.trim().isEmpty()) {
            String requestorLanguage = LanguageMappingUtil.mapLanguageToRequestorLanguage(language);
            headers.put("requestorLanguage", requestorLanguage);
            logger.info("Set requestorLanguage to: {} based on language header: {}", requestorLanguage, language);
        }
        
        // Inline former orchestrator steps: validate then generate
        requestValidatorService.validateRequest(serviceName, request);
        String xmlContent = xmlGeneratorService.generateXmlFromXsd(serviceName, request, headers);
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
