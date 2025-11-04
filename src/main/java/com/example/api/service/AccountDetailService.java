package com.example.api.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.dto.AccountDetailsResponse;

import java.util.Map;

public interface AccountDetailService {
    ApiResponse<AccountDetailsResponse> validateRequest(BaseServiceRequest baseServiceRequest);
    ApiResponse<AccountDetailsResponse> postProcessAccountDetails(ApiResponse<Map<String, Object>> response, String lang, BaseServiceRequest baseServiceRequest);
}

