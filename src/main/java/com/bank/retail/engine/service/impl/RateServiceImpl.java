package com.bank.retail.engine.service.impl;

import com.bank.retail.api.dto.ApiResponse;
import com.bank.retail.api.dto.ExchangeRateItem;
import com.bank.retail.engine.service.RateService;
import com.bank.retail.engine.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RateServiceImpl implements RateService {

    private static final Logger logger = LoggerFactory.getLogger(RateServiceImpl.class);
    
    @Value("${app.rate.digits:6}")
    private int rateDigits;

    private final CurrencyService currencyService;

    public RateServiceImpl(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public ApiResponse<ExchangeRateItem> postProcessExchangeRate(ApiResponse<Map<String, Object>> response) {
        logger.debug("Post-processing exchange rate response");
        
        try {
            if (response == null || response.getStatus() == null || response.getData() == null) {
                logger.debug("Response is null or missing data, returning empty status");
                return emptyWithStatus(response);
            }

            String code = response.getStatus().getCode();
            if (!"000000".equals(code)) {
                logger.debug("Response status code is not success: {}", code);
                return emptyWithStatus(response);
            }

            logger.debug("Processing {} exchange rate items", response.getData().size());
            List<ExchangeRateItem> transformed = new ArrayList<>();

            // Load all enabled currencies once (no need to scan response)
            logger.debug("Loading all enabled currencies for mapping");
            Map<String, Map<String, Object>> dbByIso = currencyService.findAllEnabled();

            for (Map<String, Object> item : response.getData()) {
                if (item == null) {
                    continue;
                }
                ExchangeRateItem mapped = new ExchangeRateItem();
                String isoCode = valueAsString(firstNonNull(item.get("ISOCode"), item.get("isoCode")));
                Map<String, Object> db = (isoCode == null) ? null : dbByIso.get(isoCode.toUpperCase());

                mapped.setIsoCode(valueAsString(firstNonNull(db == null ? null : db.get("isoCode"), firstNonNull(item.get("ISOCode"), item.get("isoCode")))));
                mapped.setIsoCodeNum(valueAsString(firstNonNull(db == null ? null : db.get("isoCodeNum"), firstNonNull(item.get("ISOCodeNum"), item.get("isoCodeNum")))));
                mapped.setCurNameEN(valueAsString(firstNonNull(db == null ? null : db.get("curNameEN"), firstNonNull(item.get("CurNameEN"), item.get("curNameEN"), item.get("CurName")))));
                mapped.setShortCurNameEN(valueAsString(firstNonNull(db == null ? null : db.get("shortCurNameEN"), firstNonNull(item.get("ShortCurNameEN"), item.get("shortCurNameEN")))));
                mapped.setCurNameAR(valueAsString(firstNonNull(db == null ? null : db.get("curNameAR"), firstNonNull(item.get("CurNameAR"), item.get("curNameAR")))));
                mapped.setShortCurNameAR(valueAsString(firstNonNull(db == null ? null : db.get("shortCurNameAR"), firstNonNull(item.get("ShortCurNameAR"), item.get("shortCurNameAR")))));
                mapped.setTtBuy(valueAsString(transformTtBuy(item)));
                mapped.setTtSell(firstNonNull(item.get("TTSell"), item.get("ttSell")));
                transformed.add(mapped);
            }

            logger.debug("Successfully processed {} exchange rate items", transformed.size());
            return ApiResponse.success(transformed);
        } catch (Exception e) {
            logger.error("Error post-processing exchange rate response", e);
            throw new RuntimeException("Failed to post-process exchange rate response", e);
        }
    }

    private Object transformTtBuy(Map<String, Object> item) {
        Object ttBuyObj = firstNonNull(item.get("TTBuy"), item.get("ttBuy"));
        if (ttBuyObj == null) {
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
        return a != null ? a : b;
    }

    private String valueAsString(Object v) {
        return v == null ? null : v.toString();
    }

    private Object firstNonNull(Object... values) {
        if (values == null) return null;
        for (Object v : values) {
            if (v != null) return v;
        }
        return null;
    }

    private ApiResponse<ExchangeRateItem> emptyWithStatus(ApiResponse<?> source) {
        ApiResponse<ExchangeRateItem> out = new ApiResponse<>();
        if (source != null) {
            out.setStatus(source.getStatus());
        }
        out.setData(java.util.List.of());
        return out;
    }

    
    
}


