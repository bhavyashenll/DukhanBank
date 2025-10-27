package com.example.api.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.ExchangeRateItem;
import com.example.api.dto.ProfitRateItem;
import com.example.api.dto.GroupedProfitRateResponse;

import java.util.Map;

public interface RateService {
    ApiResponse<ExchangeRateItem> postProcessExchangeRate(ApiResponse<Map<String, Object>> response, String lang);
    ApiResponse<ProfitRateItem> postProcessProfitRate(ApiResponse<Map<String, Object>> response, String lang);
    ApiResponse<GroupedProfitRateResponse> postProcessProfitRateGrouped(ApiResponse<Map<String, Object>> response, String lang);
}
