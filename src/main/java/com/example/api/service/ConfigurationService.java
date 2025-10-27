package com.example.api.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.BaseServiceRequest;
import com.example.api.dto.ConfigurationDto;

public interface ConfigurationService {
    ApiResponse<ConfigurationDto> getRequestCallbackFields(String screenName, String lang);
}