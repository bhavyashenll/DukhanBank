package com.bank.retail.engine.service;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.ConfigurationDto;

public interface ConfigurationService {
    ApiResponse<ConfigurationDto> getRequestCallbackFields(Long screenId);
}
