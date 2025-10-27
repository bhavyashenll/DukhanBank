package com.example.api.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.api.dto.ApiResponse;
import com.example.api.entity.Configuration;
import com.example.api.entity.ConfigurationFieldOptions;
import com.example.api.repository.ConfigurationFieldOptionsRepository;
import com.example.api.repository.ConfigurationRepository;
import com.example.api.repository.ScreenDetailsRepository;
import com.example.api.service.FieldValidationService;
import com.example.api.infrastructure.ProfanityDetectionService;

/**
 * Service for validating request fields based on screen configurations
 */
@Service
public class FieldValidationServiceImpl implements FieldValidationService {

    private static final Logger logger = LoggerFactory.getLogger(FieldValidationServiceImpl.class);
    
    @Autowired
    private ConfigurationRepository configurationRepository;
    
    @Autowired
    private ConfigurationFieldOptionsRepository configurationFieldOptionsRepository;
    
    @Autowired
    private ScreenDetailsRepository screenDetailsRepository;
    
    @Autowired
    private ProfanityDetectionService profanityDetectionService;

    /**
     * Validates the input request based on field configurations for the given screen
     */
    public ApiResponse<Map<String, Object>> validateRequest(String screenName, Map<String, Object> requestData) {
        logger.info("Validating request for screen: {}", screenName);
        
        return getScreenConfigurations(screenName)
                .map(configs -> validateFields(configs, requestData, screenName))
                .orElse(createErrorResponse("Screen not found: " + screenName));
    }

    /**
     * Checks if the validation response indicates a failed validation
     */
    public boolean isValidationFailed(ApiResponse<Map<String, Object>> validationResponse) {
        return Objects.nonNull(validationResponse.getData()) && 
               !validationResponse.getData().isEmpty() &&
               validationResponse.getData().get(0).containsKey("valid") &&
               !(Boolean) validationResponse.getData().get(0).get("valid");
    }

    private Optional<List<Configuration>> getScreenConfigurations(String screenName) {
        return screenDetailsRepository.findByScreenNameIgnoreCaseAndIsActive(screenName.trim(), true)
                .map(screenDetails -> {
                    List<Configuration> configs = configurationRepository
                            .findByScreenIdAndIsActiveOrderBySequence(screenDetails.getScreenId(), true);
                    return CollectionUtils.isEmpty(configs) ? null : configs;
                });
    }

    private ApiResponse<Map<String, Object>> validateFields(List<Configuration> configurations, Map<String, Object> requestData, String screenName) {
        List<String> errors = configurations.stream()
                .flatMap(config -> validateField(config, requestData).stream())
                .collect(Collectors.toList());

        errors.addAll(validateExtraFields(configurations, requestData, screenName));
        
        // Special validation for applyProducts screen - productName is mandatory but not in screen configurations
        if ("applyProducts".equalsIgnoreCase(screenName)) {
            String productName = getFieldValue(requestData, "productName");
            if (isEmpty(productName)) {
                errors.add("productName is required");
            }
        }

        Map<String, Object> result = Map.of(
                "valid", errors.isEmpty(),
                "message", errors.isEmpty() ? "Request validation successful" : "Request validation failed",
                "errors", errors
        );

        if (errors.isEmpty()) {
            return ApiResponse.success(List.of(result));
        } else {
            return ApiResponse.badRequest(List.of(result));
        }
    }

    private List<String> validateField(Configuration config, Map<String, Object> requestData) {
        String fieldKey = config.getFieldKey();
        String value = getFieldValue(requestData, fieldKey);

        List<String> errors = new ArrayList<>();

        // 1. Check if mandatory field is missing
        if (isFieldMandatory(config.getFieldOption()) && isEmpty(value)) {
            errors.add(fieldKey + " is required");
            return errors;
        }

        if (Objects.nonNull(value)) {
            errors.addAll(validateFieldValue(config, value));
        }

        return errors;
    }

    private String getFieldValue(Map<String, Object> requestData, String fieldKey) {
        Object value = requestData.get(fieldKey);
        return Objects.nonNull(value) ? value.toString().trim() : null;
    }

    private boolean isEmpty(String value) {
        return Objects.isNull(value) || value.isEmpty();
    }

    private List<String> validateFieldValue(Configuration config, String value) {
        List<String> errors = new ArrayList<>();
        String fieldKey = config.getFieldKey();

        // 2. Check length validation
        if (!config.getFieldKey().equalsIgnoreCase("ContactNo") && !validateFieldLength(config.getFieldLength(), value, config.getFieldKey())) {
            errors.add(fieldKey + " length is invalid");
        }

        // 3. Check regex validation
        if (!validateFieldValidations(config.getFieldValidations(), value)) {
            // Use ONLY custom error message from database (err_message)
            String errorMessage = Objects.nonNull(config.getErrMessage()) && !config.getErrMessage().trim().isEmpty() 
                ? config.getErrMessage() 
                : fieldKey + " does not match validation rules";
            errors.add(errorMessage);
        }

        // 4. Check profanity for fields that require profanity checking
        if (Boolean.TRUE.equals(config.getRequiresProfanityCheck()) && profanityDetectionService.containsProfanity(value)) {
            errors.add(fieldKey + " is inappropriate");
        }

        // 5. Check combo field validation
        if (isComboField(config.getFieldType()) && !validateComboField(config.getId(), value)) {
            List<String> allowedValues = getComboFieldOptions(config.getId());
            errors.add("Field '" + fieldKey + "' has invalid value. Allowed values: " + allowedValues);
        }

        return errors;
    }

    private List<String> validateExtraFields(List<Configuration> configurations, Map<String, Object> requestData, String screenName) {
        Set<String> configuredKeys = configurations.stream()
                .map(Configuration::getFieldKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return requestData.keySet().stream()
                .filter(key -> !configuredKeys.contains(key))
                .filter(key -> {
                    // For applyProducts screen, exclude productName from extra fields validation
                    // since it's mandatory but not in screen configurations
                    if ("applyProducts".equalsIgnoreCase(screenName) && "productName".equalsIgnoreCase(key)) {
                        return false;
                    }
                    return true;
                })
                .map(key -> "Field '" + key + "' is not defined in screen configuration")
                .collect(Collectors.toList());
    }

    private ApiResponse<Map<String, Object>> createErrorResponse(String message) {
        Map<String, Object> result = Map.of(
                "valid", false,
                "message", message,
                "errors", List.of(message)
        );
        return ApiResponse.success(List.of(result));
    }
    
    private boolean isFieldMandatory(String fieldOption) {
        return Optional.ofNullable(fieldOption)
                .map(String::trim)
                .map(String::toUpperCase)
                .map(option -> "M".equals(option) || "MANDATORY".equals(option) || "REQUIRED".equals(option))
                .orElse(false);
    }
    
    
    private boolean validateFieldLength(String fieldLength, String value, String fieldKey) {
        if (Objects.isNull(fieldLength) || Objects.isNull(value)) return true;
        
        return Optional.of(fieldLength.trim())
                .map(this::parseInt)
                .map(maxLength -> {
                    // For ContactNo field, use exact length match
                    if (isContactNoField(fieldKey)) {
                        return value.length() == maxLength;
                    }
                    // For other fields, use normal ≤ length validation
                    return value.length() <= maxLength;
                })
                .orElse(true);
    }
    
    /**
     * Determines if the field is ContactNo (for exact length validation)
     */
    private boolean isContactNoField(String fieldKey) {
        if (Objects.isNull(fieldKey)) return false;
        String lowerFieldKey = fieldKey.toLowerCase();
        return lowerFieldKey.contains("contactno") || 
               lowerFieldKey.contains("contact_no") ||
               lowerFieldKey.contains("mobilenumber") ||
               lowerFieldKey.contains("mobile_number");
    }
    
    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid field length configuration: {}", value);
            return Integer.MAX_VALUE; // Don't enforce invalid length
        }
    }
    
    private boolean validateFieldValidations(String fieldValidations, String value) {
        if (Objects.isNull(fieldValidations) || fieldValidations.trim().isEmpty() || Objects.isNull(value)) {
            return true;
        }
        
        return Arrays.stream(fieldValidations.split(","))
                .map(String::trim)
                .filter(rule -> !rule.isEmpty())
                .allMatch(rule -> validateRule(rule, value));
    }
    
    private boolean validateRule(String rule, String value) {
        try {
            if (rule.startsWith("regex:")) {
                String pattern = rule.substring(6).trim();
                return value.matches(pattern);
            }
            if (isRegexPattern(rule)) {
                // Clean and normalize the regex pattern
                String cleanPattern = cleanRegexPattern(rule);
                return value.matches(cleanPattern);
            }
            if (rule.startsWith("minLength:")) {
                return value.length() >= parseInt(rule.substring(10).trim());
            }
            if (rule.startsWith("maxLength:")) {
                return value.length() <= parseInt(rule.substring(10).trim());
            }
            return true;
        } catch (Exception e) {
            logger.warn("Error validating rule '{}' with value '{}': {}", rule, value, e.getMessage());
            return false;
        }
    }
    
    private boolean isRegexPattern(String rule) {
        // Check for common regex patterns
        return rule.startsWith("[") || 
               rule.startsWith("^") || 
               rule.contains("+") || 
               rule.contains("*") ||
               rule.contains("\\.") ||
               rule.contains("\\d") ||
               rule.contains("\\w") ||
               rule.contains("\\s") ||
               rule.contains("\\[") ||
               rule.contains("\\]") ||
               rule.contains("\\{") ||
               rule.contains("\\}") ||
               rule.contains("\\(") ||
               rule.contains("\\)") ||
               rule.contains("\\|") ||
               rule.contains("\\^") ||
               rule.contains("\\$");
    }
    
    private String cleanRegexPattern(String rule) {
        if (Objects.isNull(rule) || rule.trim().isEmpty()) {
            return rule;
        }
        
        String cleaned = rule.trim();
        
        // Fix common database issues with regex patterns
        // 1. Fix double backslashes that might come from database storage
        cleaned = cleaned.replace("\\\\", "\\");
        
        // 2. Fix common truncation issues by detecting incomplete patterns
        cleaned = fixIncompleteRegex(cleaned);
        
        // 3. Validate that the pattern is syntactically correct
        try {
            java.util.regex.Pattern.compile(cleaned);
            return cleaned;
        } catch (java.util.regex.PatternSyntaxException e) {
            logger.error("Invalid regex pattern after cleaning: '{}'. Error: {}", cleaned, e.getMessage());
            // Return the original pattern if cleaning fails
            return rule;
        }
    }
    
    private String fixIncompleteRegex(String pattern) {
        // Generic approach to fix common regex truncation issues
        String fixed = pattern;
        
        // Fix incomplete quantifiers like {2, {2,} without closing }
        if (fixed.contains("{") && !fixed.contains("}")) {
            // Find the last { and add appropriate closing
            int lastBrace = fixed.lastIndexOf("{");
            String beforeBrace = fixed.substring(0, lastBrace);
            String afterBrace = fixed.substring(lastBrace + 1);
            
            // Try to complete common patterns
            if (afterBrace.matches("\\d+$")) {
                // Pattern like {2 -> make it {2,}
                fixed = beforeBrace + "{" + afterBrace + ",}";
            } else if (afterBrace.matches("\\d+,$")) {
                // Pattern like {2, -> make it {2,}
                fixed = beforeBrace + "{" + afterBrace + "}";
            }
        }
        
        // Fix patterns that start with ^ but don't end with $
        if (fixed.startsWith("^") && !fixed.endsWith("$")) {
            // Check if it looks like a complete pattern that just needs $
            if (fixed.contains("+") || fixed.contains("*") || fixed.contains("?")) {
                fixed = fixed + "$";
            }
        }
        
        return fixed;
    }
    
    
    private boolean isComboField(String fieldType) {
        return Optional.ofNullable(fieldType)
                .map(String::trim)
                .map(String::toLowerCase)
                .map(type -> "combo".equals(type) || "dropdown".equals(type) || "select".equals(type))
                .orElse(false);
    }
    
    private boolean validateComboField(Long configurationId, String value) {
        return getComboFieldOptions(configurationId).contains(value);
    }
    
    private List<String> getComboFieldOptions(Long configurationId) {
        return configurationFieldOptionsRepository
                .findByConfigurationIdAndIsActive(configurationId, true)
                .stream()
                .map(ConfigurationFieldOptions::getEnglishOptionValue)
                .collect(Collectors.toList());
    }

}