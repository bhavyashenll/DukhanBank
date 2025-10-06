package com.dukhan.MQ.Helpers.controller;

import com.dukhan.MQ.Helpers.dto.ApiResponse;
import com.dukhan.MQ.Helpers.dto.ExchangeRateItem;
import com.dukhan.MQ.Helpers.service.MQServiceOrchestrator;
import com.dukhan.MQ.Helpers.service.RateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RateController {

    private static final Logger logger = LoggerFactory.getLogger(RateController.class);

    private final MQServiceOrchestrator orchestrator;
    private final RateService rateService;

    @Autowired
    public RateController(MQServiceOrchestrator orchestrator, RateService rateService) {
        this.orchestrator = orchestrator;
        this.rateService = rateService;
    }

    @GetMapping("/profitRate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfitRate() {
        final String serviceName = "PROFIT.RATE";
        logger.info("Received request for {}", serviceName);

        ApiResponse<Map<String, Object>> response = orchestrator.processEndToEndFlow(serviceName, Map.of());
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/exchangeRate", "/exchangeRate/{currencyCode}"})
    public ResponseEntity<ApiResponse<ExchangeRateItem>> getExchangeRate(@PathVariable(value = "currencyCode", required = false) String currencyCode) {
        final String serviceName = "EXCHANGE.RATE";
        logger.info("Received request for {} with currencyCode={}", serviceName, currencyCode);

        Map<String, Object> requestMap = (currencyCode == null || currencyCode.isBlank())
                ? Map.of()
                : Map.of("currencyCode", currencyCode);

        ApiResponse<Map<String, Object>> response = orchestrator.processEndToEndFlow(serviceName, requestMap);
        ApiResponse<ExchangeRateItem> transformed = rateService.postProcessExchangeRate(response);
        return ResponseEntity.ok(transformed);
    }
}


