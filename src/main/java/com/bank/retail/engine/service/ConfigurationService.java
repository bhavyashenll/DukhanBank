package com.bank.retail.engine.service;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.ConfigurationDto;
import com.bank.retail.api.dto.RequestHeadersDto;

public interface ConfigurationService {
    ApiResponse<ConfigurationDto> getRequestCallbackFields(Long screenId, RequestHeadersDto headers);
}
