package com.example.api.service.impl;

import com.example.api.dto.ApiResponse;
import com.example.api.entity.ProductAccount;
import com.example.api.entity.ProfitRateLabel;
import com.example.api.service.CurrencyService;
import com.example.api.service.ProductAccountsService;
import com.example.api.service.ProfitRateLabelService;
import com.example.api.service.RateService;
import com.example.api.dto.ExchangeRateItem;
import com.example.api.dto.GroupedProfitRateResponse;
import com.example.api.dto.ProfitRateItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RateServiceImpl implements RateService {

    private static final Logger logger = LoggerFactory.getLogger(RateServiceImpl.class);
    
    @Value("${app.rate.digits:6}")
    private int rateDigits;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private ProductAccountsService productAccountsService;

    @Autowired
    private ProfitRateLabelService profitRateLabelService;

    @Override
    public ApiResponse<ExchangeRateItem> postProcessExchangeRate(ApiResponse<Map<String, Object>> response, String lang) {
        return postProcessExchangeRate(response, lang, null);
    }

    @Override
    public ApiResponse<ExchangeRateItem> postProcessExchangeRate(ApiResponse<Map<String, Object>> response, String lang, String currencyCode) {
        logger.info("Post-processing exchange rate response for language: {}, currencyCode: {}", lang, currencyCode);
        
        // Validate input parameters
        if (Objects.isNull(lang) || lang.trim().isEmpty()) {
            throw new IllegalArgumentException("Language is required");
        }
        
        // Normalize language code
        String normalizedLang = lang.trim().toUpperCase();
        if (!"EN".equals(normalizedLang) && !"AR".equals(normalizedLang)) {
            throw new IllegalArgumentException("Invalid language code. Supported values: EN, AR");
        }
        
        // Normalize currencyCode if provided
        String normalizedCurrencyCode = null;
        if (Objects.nonNull(currencyCode) && !currencyCode.trim().isEmpty()) {
            normalizedCurrencyCode = currencyCode.trim().toUpperCase();
            logger.info("Filtering exchange rates by currencyCode: {}", normalizedCurrencyCode);
        }
        
        try {
            if (Objects.isNull(response) || Objects.isNull(response.getStatus()) || Objects.isNull(response.getData())) {
                logger.info("Response is null or missing data, returning empty status");
                return emptyWithStatus(response);
            }

            String code = response.getStatus().getCode();
            if (!"000000".equals(code)) {
                logger.info("Response status code is not success: {}", code);
                return emptyWithStatus(response);
            }

            logger.info("Processing {} exchange rate items", response.getData().size());
            List<ExchangeRateItem> transformed = new ArrayList<>();

            // Load all enabled currencies once (no need to scan response)
            logger.info("Loading all enabled currencies for mapping");
            Map<String, Map<String, Object>> dbByIso = currencyService.findAllEnabled();
			logger.info(" found currencies size = {} ", dbByIso.size());
            for (Map<String, Object> item : response.getData()) {
                try {
                    ExchangeRateItem transformedItem = transformExchangeRateItem(item, dbByIso, normalizedLang);
                    if (Objects.nonNull(transformedItem)) {
                        // Filter by currencyCode if provided
                        if (normalizedCurrencyCode == null || 
                            (Objects.nonNull(transformedItem.getIsoCode()) && 
                             transformedItem.getIsoCode().toUpperCase().equals(normalizedCurrencyCode))) {
                            transformed.add(transformedItem);
                        } else {
                            logger.debug("Filtered out exchange rate item with isoCode: {} (does not match currencyCode: {})", 
                                transformedItem.getIsoCode(), normalizedCurrencyCode);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error transforming exchange rate item: {}", e.getMessage());
                }
            }

            logger.info("Successfully processed {} exchange rate items{}", 
                transformed.size(), 
                normalizedCurrencyCode != null ? " (filtered by currencyCode: " + normalizedCurrencyCode + ")" : "");
            return ApiResponse.success(transformed);
        } catch (Exception e) {
            logger.error("Error post-processing exchange rate response", e);
            throw new RuntimeException("Failed to post-process exchange rate response", e);
        }
    }

    private ExchangeRateItem transformExchangeRateItem(Map<String, Object> item, Map<String, Map<String, Object>> dbByIso, String lang) {
        if (Objects.isNull(item)) {
            return null;
        }
        
        String isoCode = valueAsString(firstNonNull(item.get("ISOCode"), item.get("isocode")));
		logger.info("ISO code found {} ", isoCode);
        if (Objects.isNull(isoCode)) {
            logger.info("No ISO code found in item, skipping");
            return null;
        }

        // Get currency data from database
        Map<String, Object> currencyData = dbByIso.get(isoCode.toUpperCase());
        if (Objects.isNull(currencyData)) {
            logger.info("No currency data found for ISO code: {}", isoCode);
            return null;
        }

        ExchangeRateItem dto = new ExchangeRateItem();
        dto.setIsoCode(isoCode);
        dto.setIsoCodeNum(valueAsString(currencyData.get("isoCodeNum")));
        
        // Set language-specific currency names
        if ("AR".equals(lang)) {
            dto.setCurName(valueAsString(currencyData.get("curNameAR")));
            dto.setShortCurName(valueAsString(currencyData.get("shortCurNameAR")));
        } else {
            dto.setCurName(valueAsString(currencyData.get("curNameEN")));
            dto.setShortCurName(valueAsString(currencyData.get("shortCurNameEN")));
        }
        
        dto.setTtBuy(valueAsString(transformTtBuy(item)));
        dto.setTtSell(valueAsString(firstNonNull(item.get("TTSell"), item.get("ttSell"))));

        return dto;
    }

    private Object transformTtBuy(Map<String, Object> item) {
        Object ttBuyObj = firstNonNull(item.get("TTBuy"), item.get("ttBuy"));
        if (Objects.isNull(ttBuyObj)) {
            return null;
        }
        try {
            BigDecimal ttBuy = new BigDecimal(ttBuyObj.toString());
            // Avoid division by zero
            if (ttBuy.compareTo(BigDecimal.ZERO) == 0) {
                return ttBuyObj;
            }

            // Compute reciprocal and round to configured digits
            BigDecimal reciprocal = BigDecimal.ONE.divide(ttBuy, rateDigits + 4, RoundingMode.HALF_UP);
            BigDecimal finalValue = reciprocal.setScale(rateDigits, RoundingMode.HALF_UP);
            return finalValue.toPlainString();
        } catch (Exception ex) {
            return ttBuyObj;
        }
    }

    private Object firstNonNull(Object a, Object b) {
        return Objects.nonNull(a) ? a : b;
    }

    private String valueAsString(Object v) {
        return Objects.isNull(v) ? null : v.toString();
    }

    private ApiResponse<ExchangeRateItem> emptyWithStatus(ApiResponse<?> source) {
        ApiResponse<ExchangeRateItem> out = new ApiResponse<>();
        if (Objects.nonNull(source)) {
            out.setStatus(source.getStatus());
        }
        out.setData(java.util.List.of());
        return out;
    }

    @Override
    public ApiResponse<ProfitRateItem> postProcessProfitRate(ApiResponse<Map<String, Object>> response, String lang) {
        logger.info("Post-processing profit rate response for language: {}", lang);
        
        // Validate input parameters
        if (Objects.isNull(lang) || lang.trim().isEmpty()) {
            throw new IllegalArgumentException("Language is required");
        }
        
        // Normalize language code
        String normalizedLang = lang.trim().toUpperCase();
        if (!"EN".equals(normalizedLang) && !"AR".equals(normalizedLang)) {
            throw new IllegalArgumentException("Invalid language code. Supported values: EN, AR");
        }
        
        try {
            if (Objects.isNull(response) || Objects.isNull(response.getStatus()) || Objects.isNull(response.getData())) {
                logger.info("Response is null or missing data, returning empty status");
                return emptyProfitRateWithStatus(response);
            }

            String code = response.getStatus().getCode();
            if (!"000000".equals(code)) {
                logger.info("Response status code is not success: {}", code);
                return emptyProfitRateWithStatus(response);
            }

            logger.info("Processing {} profit rate items", response.getData().size());
            
            // Step 1: Fetch all active profit rate labels (cached and efficient)
            Map<String, ProfitRateLabel> profitRateLabelMap = profitRateLabelService.findAllActive();
            logger.info("Loaded {} profit rate labels from database", profitRateLabelMap.size());

            List<ProfitRateItem> transformed = new ArrayList<>();

            for (Map<String, Object> item : response.getData()) {
                try {
                    ProfitRateItem transformedItem = transformProfitRateItemWithLabels(item, normalizedLang, profitRateLabelMap);
                    if (Objects.nonNull(transformedItem)) {
                        transformed.add(transformedItem);
                    }
                } catch (Exception e) {
                    logger.warn("Error transforming profit rate item: {}", e.getMessage());
                }
            }

            logger.info("Successfully processed {} profit rate items", transformed.size());
            return ApiResponse.success(transformed);
        } catch (Exception e) {
            logger.error("Error post-processing profit rate response", e);
            throw new RuntimeException("Failed to post-process profit rate response", e);
        }
    }


    private ProfitRateItem transformProfitRateItemWithLabels(Map<String, Object> item, String lang, Map<String, ProfitRateLabel> profitRateLabelMap) {
        if (Objects.isNull(item)) {
            return null;
        }
        
        // Extract all fields from the original response
        String lastMonthDate = valueAsString(item.get("lastMonthDate"));
        String originalDescription = valueAsString(item.get("description"));
        String rate = valueAsString(item.get("rate"));
        String rateCreationDate = valueAsString(item.get("rateCreationDate"));
        String productType = valueAsString(item.get("productType"));
        String productSubtype = valueAsString(item.get("productSubtype"));
        String tenure = valueAsString(item.get("tenure"));
        String currency = valueAsString(item.get("currency"));
        
        if (Objects.isNull(productType) || Objects.isNull(productSubtype) || Objects.isNull(currency)) {
            logger.info("Missing required fields in profit rate item, skipping");
            return null;
        }

        // Get product account data from database - if not found, exclude this item
        Optional<ProductAccount> productAccountOpt = productAccountsService.findByTypeAndClassCodeAndCurrency(
            productType, productSubtype, currency);
        
        // If no database match found, exclude this profit rate item from response
        if (productAccountOpt.isEmpty()) {
            logger.info("No product account found for type: {}, classCode: {}, currency: {} - excluding from response", 
                productType, productSubtype, currency);
            return null;
        }
        
        ProductAccount productAccount = productAccountOpt.get();
        String description = originalDescription; // Use original description as default
        
        // Override description with database value based on language
        if ("AR".equals(lang)) {
            String dbDescription = productAccount.getArabicDescription();
            if (Objects.nonNull(dbDescription) && !dbDescription.trim().isEmpty()) {
                description = dbDescription;
            }
        } else {
            String dbDescription = productAccount.getEnglishDescription();
            if (Objects.nonNull(dbDescription) && !dbDescription.trim().isEmpty()) {
                description = dbDescription;
            }
        }

        // Get profit rate label for productType
        String productTypeLabel = productType; // Default to original productType
        ProfitRateLabel profitRateLabel = profitRateLabelMap.get(productType.toUpperCase());
        if (Objects.nonNull(profitRateLabel)) {
            if ("AR".equals(lang)) {
                String arabicLabel = profitRateLabel.getArabicLabel();
                if (Objects.nonNull(arabicLabel) && !arabicLabel.trim().isEmpty()) {
                    productTypeLabel = arabicLabel;
                }
            } else {
                String englishLabel = profitRateLabel.getEnglishLabel();
                if (Objects.nonNull(englishLabel) && !englishLabel.trim().isEmpty()) {
                    productTypeLabel = englishLabel;
                }
            }
        }

        // Create DTO with all fields from original response
        ProfitRateItem dto = new ProfitRateItem();
        dto.setLastMonthDate(lastMonthDate);
        dto.setDescription(description); // This might be overridden from DB
        dto.setRate(rate);
        dto.setRateCreationDate(rateCreationDate);
        dto.setProductType(productTypeLabel); // Use the label instead of original productType
        dto.setProductSubtype(productSubtype);
        dto.setTenure(tenure);
        dto.setCurrency(currency);

        return dto;
    }

    private ApiResponse<ProfitRateItem> emptyProfitRateWithStatus(ApiResponse<?> source) {
        ApiResponse<ProfitRateItem> out = new ApiResponse<>();
        if (Objects.nonNull(source)) {
            out.setStatus(source.getStatus());
        }
        out.setData(java.util.List.of());
        return out;
    }

    @Override
    public ApiResponse<GroupedProfitRateResponse> postProcessProfitRateGrouped(ApiResponse<Map<String, Object>> response, String lang) {
        logger.info("Post-processing grouped profit rate response for language: {}", lang);
        
        // Validate input parameters
        if (Objects.isNull(lang) || lang.trim().isEmpty()) {
            throw new IllegalArgumentException("Language is required");
        }
        
        // Normalize language code
        String normalizedLang = lang.trim().toUpperCase();
        if (!"EN".equals(normalizedLang) && !"AR".equals(normalizedLang)) {
            throw new IllegalArgumentException("Invalid language code. Supported values: EN, AR");
        }
        
        try {
            if (Objects.isNull(response) || Objects.isNull(response.getStatus()) || Objects.isNull(response.getData())) {
                logger.info("Response is null or missing data, returning empty status");
                return emptyGroupedProfitRateWithStatus(response);
            }

            String code = response.getStatus().getCode();
            if (!"000000".equals(code)) {
                logger.info("Response status code is not success: {}", code);
                return emptyGroupedProfitRateWithStatus(response);
            }

            logger.info("Processing {} profit rate items for grouping", response.getData().size());
            
            // Step 1: Collect all unique type:classCode:currency combinations for batch DB query
            List<String> typeClassCodeCurrencyList = response.getData().stream()
                .map(item -> {
                    String productType = valueAsString(item.get("productType"));
                    String productSubtype = valueAsString(item.get("productSubtype"));
                    String currency = valueAsString(item.get("currency"));
                    return (Objects.nonNull(productType) && Objects.nonNull(productSubtype) && Objects.nonNull(currency)) 
                        ? productType + ":" + productSubtype + ":" + currency 
                        : null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

            logger.info("Found {} unique type:classCode:currency combinations", typeClassCodeCurrencyList.size());

            // Step 2: Fetch all active profit rate labels (cached and efficient)
            Map<String, ProfitRateLabel> profitRateLabelMap = profitRateLabelService.findAllActive();
            logger.info("Loaded {} profit rate labels from database", profitRateLabelMap.size());

            // Step 3: Single batch database query for product accounts
            Map<String, ProductAccount> productAccountMap = productAccountsService.findByTypeAndClassCodeAndCurrencyBatch(typeClassCodeCurrencyList);
            logger.info("Found {} product accounts from database", productAccountMap.size());

            // Step 4: Process all items and group by original productType code
            Map<String, List<ProfitRateItem>> groupedProfitRates = response.getData().stream()
                .map(item -> transformProfitRateItemWithBatchAndLabelsForGrouping(item, normalizedLang, productAccountMap, profitRateLabelMap))
                .filter(Objects::nonNull) // Only include items that have database matches
                .collect(Collectors.groupingBy(profitRateItem -> {
                    // Group by original product type code, not the translated label
                    return profitRateItem.getOriginalProductType();
                }));

            logger.info("Successfully processed and grouped {} profit rate items into {} product types", 
                groupedProfitRates.values().stream().mapToInt(List::size).sum(), 
                groupedProfitRates.size());

            GroupedProfitRateResponse groupedResponse = new GroupedProfitRateResponse(groupedProfitRates);
            return ApiResponse.success(List.of(groupedResponse));
            
        } catch (Exception e) {
            logger.error("Error post-processing grouped profit rate response", e);
            throw new RuntimeException("Failed to post-process grouped profit rate response", e);
        }
    }



    private ProfitRateItem transformProfitRateItemWithBatchAndLabelsForGrouping(Map<String, Object> item, String lang, Map<String, ProductAccount> productAccountMap, Map<String, ProfitRateLabel> profitRateLabelMap) {
        if (Objects.isNull(item)) {
            return null;
        }
        
        // Extract all fields from the original response
        String lastMonthDate = valueAsString(item.get("lastMonthDate"));
        String originalDescription = valueAsString(item.get("description"));
        String rate = valueAsString(item.get("rate"));
        String rateCreationDate = valueAsString(item.get("rateCreationDate"));
        String productType = valueAsString(item.get("productType"));
        String productSubtype = valueAsString(item.get("productSubtype"));
        String tenure = valueAsString(item.get("tenure"));
        String currency = valueAsString(item.get("currency"));
        
        if (Objects.isNull(productType) || Objects.isNull(productSubtype) || Objects.isNull(currency)) {
            logger.info("Missing required fields in profit rate item, skipping");
            return null;
        }

        // Check if we have database match using the batch result
        String key = productType + ":" + productSubtype + ":" + currency;
        ProductAccount productAccount = productAccountMap.get(key);
        
        // If no database match found, exclude this profit rate item from response
        if (Objects.isNull(productAccount)) {
            logger.info("No product account found for key: {} - excluding from response", key);
            return null;
        }
        
        String description = originalDescription; // Use original description as default
        
        // Override description with database value based on language
        if ("AR".equals(lang)) {
            String dbDescription = productAccount.getArabicDescription();
            if (Objects.nonNull(dbDescription) && !dbDescription.trim().isEmpty()) {
                description = dbDescription;
            }
        } else {
            String dbDescription = productAccount.getEnglishDescription();
            if (Objects.nonNull(dbDescription) && !dbDescription.trim().isEmpty()) {
                description = dbDescription;
            }
        }

        // Get profit rate label for productType
        String productTypeLabel = productType; // Default to original productType
        ProfitRateLabel profitRateLabel = profitRateLabelMap.get(productType.toUpperCase());
        if (Objects.nonNull(profitRateLabel)) {
            if ("AR".equals(lang)) {
                String arabicLabel = profitRateLabel.getArabicLabel();
                if (Objects.nonNull(arabicLabel) && !arabicLabel.trim().isEmpty()) {
                    productTypeLabel = arabicLabel;
                }
            } else {
                String englishLabel = profitRateLabel.getEnglishLabel();
                if (Objects.nonNull(englishLabel) && !englishLabel.trim().isEmpty()) {
                    productTypeLabel = englishLabel;
                }
            }
        }

        // Create DTO with all fields from original response
        ProfitRateItem dto = new ProfitRateItem();
        dto.setLastMonthDate(lastMonthDate);
        dto.setDescription(description); // This might be overridden from DB
        dto.setRate(rate);
        dto.setRateCreationDate(rateCreationDate);
        dto.setProductType(productTypeLabel); // Use the label instead of original productType
        dto.setProductSubtype(productSubtype);
        dto.setTenure(tenure);
        dto.setCurrency(currency);
        dto.setOriginalProductType(productType); // Store original for grouping

        return dto;
    }

    private ApiResponse<GroupedProfitRateResponse> emptyGroupedProfitRateWithStatus(ApiResponse<?> source) {
        ApiResponse<GroupedProfitRateResponse> out = new ApiResponse<>();
        if (Objects.nonNull(source)) {
            out.setStatus(source.getStatus());
        }
        GroupedProfitRateResponse emptyResponse = new GroupedProfitRateResponse();
        out.setData(List.of(emptyResponse));
        return out;
    }
}


