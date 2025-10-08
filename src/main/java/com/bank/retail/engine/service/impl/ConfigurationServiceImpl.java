package com.bank.retail.engine.service.impl;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.ConfigurationDto;
import com.bank.retail.api.dto.RequestHeadersDto;
import com.bank.retail.persistence.entity.Configuration;
import com.bank.retail.engine.service.ConfigurationService;
import com.bank.retail.persistence.repository.ConfigurationRepository;
import com.bank.retail.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public ApiResponse<ConfigurationDto> getRequestCallbackFields(Long screenId, RequestHeadersDto headers) {
        if (screenId == null || screenId <= 0) {
            throw new BusinessException("Invalid screen ID provided", "INVALID_SCREEN_ID");
        }
        
        if (headers == null) {
            throw new BusinessException("Request headers cannot be null", "MISSING_HEADERS");
        }
        
        try {
            List<Configuration> configurations = configurationRepository.findCallbackFields(screenId);
            
            if (CollectionUtils.isEmpty(configurations)) {
                return ApiResponse.noDataFound();
            }
            
            List<ConfigurationDto> fields = configurations.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fields);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Failed to retrieve callback fields for screen ID: " + screenId, "DATABASE_ERROR", e);
        }
    }

    private ConfigurationDto mapToDto(Configuration c) {
        ConfigurationDto dto = new ConfigurationDto(
                c.getFieldName(),
                c.getFieldOption(),
                c.getFieldLength(),
                c.getFieldValidations(),
                c.getFieldType(),
                c.getSequence()
        );
        if (c.getFieldType() != null) {
            String type = c.getFieldType().trim();
            if ("combo".equalsIgnoreCase(type) || "dropdown".equalsIgnoreCase(type)) {
                dto.setFieldOptions(c.getFieldOptions());
            }
        }
        return dto;
    }
}
