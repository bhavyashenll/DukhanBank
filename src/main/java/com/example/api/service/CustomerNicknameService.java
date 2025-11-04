package com.example.api.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;

public interface CustomerNicknameService {

    ApiResponse<Object> getCustomerNickname(BaseServiceRequest request);
    ApiResponse<Object> upsertCustomerNickname(BaseServiceRequest request);
    ApiResponse<Object> deleteCustomerNickname(BaseServiceRequest request);
}
