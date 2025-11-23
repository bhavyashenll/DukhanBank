package com.example.api.controller;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.dto.ExchangeRateItem;
import com.example.api.dto.GroupedProfitRateResponse;
import com.example.api.infrastructure.AppConstant;
import com.example.api.infrastructure.RequireDeviceInfo;
import com.example.api.infrastructure.ResponseConstant;
import com.example.api.service.ProcessUIRequest;
import com.example.api.service.RateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.api.infrastructure.PathParamHelper;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequireDeviceInfo
public class RateController {

    private static final Logger logger = LoggerFactory.getLogger(RateController.class);

    @Autowired
    private ProcessUIRequest processUIRequest;

    @Autowired
    private PathParamHelper pathParamHelper;

    @Autowired
    private RateService rateService;

    @PostMapping("/view-profit-rates")
    public ResponseEntity<ApiResponse<GroupedProfitRateResponse>> getProfitRate(
            @RequestHeader(name = AppConstant.HEADER_CHANNEL) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, defaultValue = AppConstant.DEFAULT_LANGUAGE) String lang,
            @RequestHeader(name = AppConstant.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID) String screenId,
            @RequestHeader(name = AppConstant.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        logger.info("Received profitRate request | serviceId={}, screenId={}, lang={}", serviceId, screenId, lang);

        ApiResponse<Map<String, Object>> response =
                processUIRequest.processEndToEndFlow("PROFIT.RATE", baseServiceRequest, screenId);

        logger.info(" processEndToEndFlow completed | statusCode={}, description={}",
                response.getStatus() != null ? response.getStatus().getCode() : "NULL",
                response.getStatus() != null ? response.getStatus().getDescription() : "NULL");

        ApiResponse<GroupedProfitRateResponse> transformed =
                rateService.postProcessProfitRateGrouped(response, lang);

        logger.info(" After postProcessProfitRateGrouped | statusCode={}, description={}",
                transformed.getStatus() != null ? transformed.getStatus().getCode() : "NULL",
                transformed.getStatus() != null ? transformed.getStatus().getDescription() : "NULL");

        if (transformed.getStatus() != null &&
                ResponseConstant.SUCCESS_CODE.equals(transformed.getStatus().getCode())) {
            transformed.getStatus().setDescription(ResponseConstant.process_msg);
            logger.info(" Updated final response description to '{}'", ResponseConstant.process_msg);
        } else {
            logger.warn("Skipped setting process message due to non-success status");
        }

        logger.info("Returning final profitRate response | code={}, description={}",
                transformed.getStatus() != null ? transformed.getStatus().getCode() : "NULL",
                transformed.getStatus() != null ? transformed.getStatus().getDescription() : "NULL");

        return ResponseEntity.ok(transformed);
    }

    @PostMapping({ "/view-fx-rates", "/view-fx-rates/{currencyCode}" })
    public ResponseEntity<ApiResponse<ExchangeRateItem>> getExchangeRate(
            @PathVariable(value = "currencyCode", required = false) String currencyCode,
            @RequestHeader(name = AppConstant.HEADER_CHANNEL) String channel,
            @RequestHeader(name = AppConstant.HEADER_ACCEPT_LANGUAGE, defaultValue = AppConstant.DEFAULT_LANGUAGE) String lang,
            @RequestHeader(name = AppConstant.SERVICEID) String serviceId,
            @RequestHeader(name = AppConstant.SCREEN_ID) String screenId,
            @RequestHeader(name = AppConstant.MODULE_ID) String moduleId,
            @RequestHeader(name = AppConstant.SUB_MODULE_ID) String subModuleId,
            @RequestBody BaseServiceRequest baseServiceRequest) {

        logger.info("Received exchangeRate request | currencyCode={}, serviceId={}, screenId={}, lang={}",
                currencyCode, serviceId, screenId, lang);

        BaseServiceRequest updatedRequest =
                pathParamHelper.createRequestWithPathParam(baseServiceRequest, "currencyCode", currencyCode);

        ApiResponse<Map<String, Object>> response =
                processUIRequest.processEndToEndFlow("EXCHANGE.RATES", updatedRequest, screenId);

        logger.info("processEndToEndFlow completed | statusCode={}, description={}",
                response.getStatus() != null ? response.getStatus().getCode() : "NULL",
                response.getStatus() != null ? response.getStatus().getDescription() : "NULL");

        ApiResponse<ExchangeRateItem> transformed =
                rateService.postProcessExchangeRate(response, lang);

        logger.info("After postProcessExchangeRate | statusCode={}, description={}",
                transformed.getStatus() != null ? transformed.getStatus().getCode() : "NULL",
                transformed.getStatus() != null ? transformed.getStatus().getDescription() : "NULL");

        if (transformed.getStatus() != null &&
                ResponseConstant.SUCCESS_CODE.equals(transformed.getStatus().getCode())) {
            transformed.getStatus().setDescription(ResponseConstant.process_msg);
            logger.info("Updated final response description to '{}'", ResponseConstant.process_msg);
        } else {
            logger.warn("Skipped setting process message due to non-success status");
        }

        logger.info(" Returning final exchangeRate response | code={}, description={}",
                transformed.getStatus() != null ? transformed.getStatus().getCode() : "NULL",
                transformed.getStatus() != null ? transformed.getStatus().getDescription() : "NULL");

        return ResponseEntity.ok(transformed);
    }
}
