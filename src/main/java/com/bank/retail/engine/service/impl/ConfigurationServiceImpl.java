package com.bank.retail.engine.service.impl;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.ConfigurationDto;
import com.bank.retail.persistence.entity.Configuration;
import com.bank.retail.engine.service.ConfigurationService;
import com.bank.retail.persistence.repository.ConfigurationRepository;
import com.bank.retail.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationServiceImpl.class);
    private final ConfigurationRepository configurationRepository;

    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public ApiResponse<ConfigurationDto> getRequestCallbackFields(Long screenId) {
        logger.info("Retrieving callback fields for screen ID: {}", screenId);
        
        if (screenId == null || screenId <= 0) {
            logger.warn("Invalid screen ID provided: {}", screenId);
            throw new BusinessException("Invalid screen ID provided", "INVALID_SCREEN_ID");
        }
        
        try {
            logger.debug("Querying database for callback fields");
            List<Configuration> configurations = configurationRepository.findCallbackFields(screenId);
            
            if (CollectionUtils.isEmpty(configurations)) {
                logger.info("No callback fields found for screen ID: {}", screenId);
                return ApiResponse.noDataFound();
            }
            
            logger.debug("Found {} callback fields for screen ID: {}", configurations.size(), screenId);
            List<ConfigurationDto> fields = configurations.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            logger.info("Successfully retrieved {} callback fields for screen ID: {}", fields.size(), screenId);
            return ApiResponse.success(fields);
            
        } catch (BusinessException e) {
            logger.error("Business exception while retrieving callback fields for screen ID: {}", screenId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving callback fields for screen ID: {}", screenId, e);
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
