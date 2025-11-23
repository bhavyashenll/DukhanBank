package com.example.api.account.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;

import java.util.Map;

public interface DownloadEstatementService {
    ApiResponse<Map<String, Object>> validateRequest(BaseServiceRequest baseServiceRequest);
    ApiResponse<Map<String, Object>> downloadEstatement(Map<String, Object> request);
}

