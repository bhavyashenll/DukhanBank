package com.bank.retail.api.controller;

import com.bank.retail.api.constants.AppConstants;
import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.ExchangeRateItem;
import com.bank.retail.engine.service.MQServiceOrchestrator;
import com.bank.retail.engine.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RateController {

    private final MQServiceOrchestrator orchestrator;
    private final RateService rateService;

    @Autowired
    public RateController(MQServiceOrchestrator orchestrator, RateService rateService) {
        this.orchestrator = orchestrator;
        this.rateService = rateService;
    }

    @GetMapping("/profitRate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfitRate(
            @RequestHeader(name = AppConstants.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstants.SCREENID) String screenId,
            @RequestHeader(name = AppConstants.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstants.SUB_MODULE_ID) String subModuleId) {
        ApiResponse<Map<String, Object>> response = orchestrator.processEndToEndFlow("PROFIT.RATE", Map.of());
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/exchangeRate", "/exchangeRate/{currencyCode}"})
    public ResponseEntity<ApiResponse<ExchangeRateItem>> getExchangeRate(
            @PathVariable(value = "currencyCode", required = false) String currencyCode,
            @RequestHeader(name = AppConstants.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstants.SCREENID) String screenId,
            @RequestHeader(name = AppConstants.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstants.SUB_MODULE_ID) String subModuleId) {
        
        Map<String, Object> requestMap = (currencyCode == null || currencyCode.isBlank())
                ? Map.of()
                : Map.of("currencyCode", currencyCode);

        ApiResponse<Map<String, Object>> response = orchestrator.processEndToEndFlow("EXCHANGE.RATE", requestMap);
        ApiResponse<ExchangeRateItem> transformed = rateService.postProcessExchangeRate(response);
        return ResponseEntity.ok(transformed);
    }
}