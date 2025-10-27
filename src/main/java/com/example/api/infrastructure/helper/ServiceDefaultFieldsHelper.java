package com.example.api.infrastructure.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Helper class for managing default fields for different services
 */
@Component
public class ServiceDefaultFieldsHelper {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDefaultFieldsHelper.class);

    /**
     * Gets default fields for a specific service
     * @param serviceName The service name
     * @return Map of default field names and values
     */
    public Map<String, Object> getDefaultFields(String serviceName) {
        logger.info("Getting default fields for service: {}", serviceName);
        
        try {
            if (Objects.isNull(serviceName)) {
                logger.warn("Service name is null, returning empty default fields");
                return Map.of();
            }

            Map<String, Object> defaultFields = switch (serviceName.toUpperCase()) {
                case "EXCHANGE.RATE" -> Map.of("indexRate", "11");
                case "PROFIT.RATE" -> Map.of();
                default -> Map.of();
            };
            
            logger.info("Found {} default fields for service: {}", defaultFields.size(), serviceName);
            return defaultFields;
        } catch (Exception e) {
            logger.error("Error getting default fields for service: {}", serviceName, e);
            return Map.of();
        }
    }

    /**
     * Checks if a service has default fields
     * @param serviceName The service name
     * @return true if the service has default fields, false otherwise
     */
    public boolean hasDefaultFields(String serviceName) {
        return !getDefaultFields(serviceName).isEmpty();
    }

    /**
     * Gets a specific default field value for a service
     * @param serviceName The service name
     * @param fieldName The field name
     * @return The default value or null if not found
     */
    public Object getDefaultFieldValue(String serviceName, String fieldName) {
        if (Objects.isNull(fieldName)) {
            logger.warn("Field name is null for service: {}", serviceName);
            return null;
        }
        Map<String, Object> defaultFields = getDefaultFields(serviceName);
        return defaultFields.get(fieldName);
    }

    /**
     * Derives serviceName from screenName based on mapping rules
     * @param screenName the screen name from header
     * @return the corresponding service name
     */
    public String deriveServiceNameFromScreenName(String screenName) {
        if (Objects.isNull(screenName)) {
            logger.warn("ScreenName is null, using default service name");
            return "DEFAULT.SERVICE";
        }
        
        String serviceName = switch (screenName.toLowerCase()) {
            case "callback" -> "CREATE.TICKET";
            case "applyproducts" -> "CREATE.LEAD";
            default -> {
                logger.warn("Unknown screenName: {}, using default service name", screenName);
                yield "DEFAULT.SERVICE";
            }
        };
        
        logger.info("Derived serviceName: {} from screenName: {}", serviceName, screenName);
        return serviceName;
    }

    /**
     * Derives serviceType from screenName based on mapping rules
     * @param screenName the screen name from header
     * @return the corresponding service type
     */
    public String deriveServiceTypeFromScreenName(String screenName) {
        if (Objects.isNull(screenName)) {
            logger.warn("ScreenName is null, using default service type");
            return "DEFAULT_SERVICE_TYPE";
        }
        
        String serviceType = switch (screenName.toLowerCase()) {
            case "applyproducts" -> "Finance_ApplyProducts";
            case "callback" -> "General_Callback";
            default -> {
                logger.warn("Unknown screenName: {}, using default service type", screenName);
                yield "DEFAULT_SERVICE_TYPE";
            }
        };
        
        logger.info("Derived serviceType: {} from screenName: {}", serviceType, screenName);
        return serviceType;
    }
}
