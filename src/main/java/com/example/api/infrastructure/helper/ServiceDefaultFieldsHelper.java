package com.example.api.infrastructure.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Helper class for managing default fields for different services
 */
@Component
public class ServiceDefaultFieldsHelper {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDefaultFieldsHelper.class);
    
    // List of services that require that doesnot return any data but only status and message
    private static final Set<String> PROCESSING_SERVICES = Set.of(
        "CRM.CREATE.TICKET", "CRM.CREATE.LEAD"
        // Add more processing services here as needed
    );

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
                case "EXCHANGE.RATE" -> {
                    Map<String, Object> exchangeRateFields = new HashMap<>();
                    exchangeRateFields.put("indexRate", "11");
                    yield exchangeRateFields;
                }
                case "PROFIT.RATE" -> Map.of();
                case "CRM.CREATE.LEAD" ->{
                    Map<String, Object> createLeadFields = new HashMap<>();
                    createLeadFields.put("typeOfBusiness", "Contracting");
                    createLeadFields.put("CustomerType", "NonBarwaCustomer");
                    createLeadFields.put("RimNo", "0");
                    createLeadFields.put("IncomingChannel", "MB");
                    yield createLeadFields;
                }
                case "TRANSACTION.STATEMENT" -> {
                    // Calculate dates: toDate = current date, fromDate = toDate - 6 months
                    LocalDate toDate = LocalDate.now();
                    LocalDate fromDate = toDate.minusMonths(6);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    Map<String, Object> dateFields = new HashMap<>();
                    dateFields.put("searchBy.period.toDate", toDate.format(formatter));
                    dateFields.put("searchBy.period.fromDate", fromDate.format(formatter));
                    yield dateFields;
                }
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
            case "callback" -> "CRM.CREATE.TICKET";
            case "applyproducts" -> "CRM.CREATE.LEAD";
            default -> {
                logger.warn("Unknown screenName: {}, using default service name", screenName);
                yield "DEFAULT.SERVICE";
            }
        };
        
        logger.info("Derived serviceName: {} from screenName: {}", serviceName, screenName);
        return serviceName;
    }

    /**
     * Derives static fields from screenName based on mapping rules
     * @param screenName the screen name from header
     * @return Map of static field names and values to be added to parameters
     */
    public Map<String, Object> deriveStaticFieldsFromScreenName(String screenName) {
        if (Objects.isNull(screenName)) {
            logger.warn("ScreenName is null, returning empty static fields");
            return Map.of();
        }
        
        Map<String, Object> staticFields = switch (screenName.toLowerCase()) {
            case "applyproducts" -> Map.of("serviceType", "Finance_ApplyProducts");
            case "callback" -> Map.of(
                "serviceType", "General_ContactRequest",
                "customerType", "NonBarwaCustomer",
                "title", "Contact Request",
                "callType", "Request"
            );
            default -> {
                logger.warn("Unknown screenName: {}, returning empty static fields", screenName);
                yield Map.of();
            }
        };
        
        logger.info("Derived static fields: {} from screenName: {}", staticFields, screenName);
        return staticFields;
    }

    /**
     * Checks if a service name requires that doesnot return any data but only status and message
     * @param serviceName The service name to check
     * @return true if the service requires processing, false otherwise
     */
    public boolean findIfProcessingService(String serviceName) {
        if (Objects.isNull(serviceName)) {
            logger.debug("Service name is null, returning false");
            return false;
        }
        
        boolean isProcessingService = PROCESSING_SERVICES.contains(serviceName);
        logger.debug("Service {} is {} a processing service", serviceName, isProcessingService ? "" : "not");
        return isProcessingService;
    }
}
