package com.example.api.account.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;

import java.util.Map;

public interface AccountTransactionService {
    ApiResponse<Map<String, Object>> validateRequest(BaseServiceRequest baseServiceRequest);
    ApiResponse<Map<String, Object>> postProcessAccountTransactions(ApiResponse<Map<String, Object>> response, BaseServiceRequest baseServiceRequest);
}

