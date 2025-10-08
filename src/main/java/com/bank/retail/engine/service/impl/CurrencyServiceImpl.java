package com.bank.retail.engine.service.impl;

import com.bank.retail.engine.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.bank.retail.persistence.entity.Currency;
import com.bank.retail.persistence.repository.CurrencyRepository;

import java.util.Map;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);
    private final CurrencyRepository currencyRepository;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Map<String, Object> findByIsoCode(String isoCode) {
        logger.debug("Finding currency by ISO code: {}", isoCode);
        
        try {
            Currency c = currencyRepository
                    .findByIsoCodeIgnoreCaseAndStatus(isoCode, "ENABLED")
                    .orElse(null);
            if (c == null) {
                logger.debug("No currency found for ISO code: {}", isoCode);
                return null;
            }
            
            logger.debug("Found currency for ISO code: {}", isoCode);
            return Map.of(
                    "isoCode", c.getIsoCode(),
                    "isoCodeNum", c.getIsoNumber(),
                    "curNameEN", c.getEnglishName(),
                    "shortCurNameEN", c.getEnglishShortName(),
                    "curNameAR", c.getArabicName(),
                    "shortCurNameAR", c.getArabicShortName()
            );
        } catch (Exception e) {
            logger.error("Error finding currency by ISO code: {}", isoCode, e);
            throw new RuntimeException("Failed to find currency by ISO code: " + isoCode, e);
        }
    }

    @Cacheable(cacheNames = "currencyByIso", key = "#isoCode?.toUpperCase()")
    public Map<String, Object> cachedByIso(String isoCode) {
        logger.debug("Cached lookup for currency by ISO code: {}", isoCode);
        return findByIsoCode(isoCode);
    }

    public Map<String, Map<String, Object>> findAllByIsoCodes(java.util.Collection<String> isoCodes) {
        logger.debug("Finding currencies by ISO codes: {}", isoCodes);
        
        if (isoCodes == null || isoCodes.isEmpty()) {
            logger.debug("No ISO codes provided, returning empty map");
            return java.util.Map.of();
        }
        
        try {
            java.util.List<Currency> rows = currencyRepository.findByIsoCodeInIgnoreCaseAndStatus(isoCodes, "ENABLED");
            logger.debug("Found {} currencies for provided ISO codes", rows.size());
            
            java.util.Map<String, Map<String, Object>> out = new java.util.HashMap<>();
            for (Currency c : rows) {
                out.put(c.getIsoCode().toUpperCase(), Map.of(
                        "isoCode", c.getIsoCode(),
                        "isoCodeNum", c.getIsoNumber(),
                        "curNameEN", c.getEnglishName(),
                        "shortCurNameEN", c.getEnglishShortName(),
                        "curNameAR", c.getArabicName(),
                        "shortCurNameAR", c.getArabicShortName()
                ));
            }
            return out;
        } catch (Exception e) {
            logger.error("Error finding currencies by ISO codes: {}", isoCodes, e);
            throw new RuntimeException("Failed to find currencies by ISO codes", e);
        }
    }

    public Map<String, Map<String, Object>> findAllEnabled() {
        logger.debug("Finding all enabled currencies");
        
        try {
            java.util.List<Currency> rows = currencyRepository.findByStatus("ENABLED");
            logger.debug("Found {} enabled currencies", rows.size());
            
            java.util.Map<String, Map<String, Object>> out = new java.util.HashMap<>();
            for (Currency c : rows) {
                out.put(c.getIsoCode().toUpperCase(), Map.of(
                        "isoCode", c.getIsoCode(),
                        "isoCodeNum", c.getIsoNumber(),
                        "curNameEN", c.getEnglishName(),
                        "shortCurNameEN", c.getEnglishShortName(),
                        "curNameAR", c.getArabicName(),
                        "shortCurNameAR", c.getArabicShortName()
                ));
            }
            return out;
        } catch (Exception e) {
            logger.error("Error finding all enabled currencies", e);
            throw new RuntimeException("Failed to find all enabled currencies", e);
        }
    }
}


