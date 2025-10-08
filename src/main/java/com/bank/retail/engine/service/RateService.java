package com.bank.retail.engine.service;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.ExchangeRateItem;

import java.util.Map;

public interface RateService {
    ApiResponse<ExchangeRateItem> postProcessExchangeRate(ApiResponse<Map<String, Object>> response);
}
