package com.dukhan.MQ.Helpers.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.dukhan.MQ.Helpers.entity.Currency;
import com.dukhan.MQ.Helpers.repository.CurrencyRepository;

import java.util.Map;

@Service
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Map<String, Object> findByIsoCode(String isoCode) {
        Currency c = currencyRepository
                .findByIsoCodeIgnoreCaseAndStatus(isoCode, "ENABLED")
                .orElse(null);
        if (c == null) return null;
        return Map.of(
                "isoCode", c.getIsoCode(),
                "isoCodeNum", c.getIsoNumber(),
                "curNameEN", c.getEnglishName(),
                "shortCurNameEN", c.getEnglishShortName(),
                "curNameAR", c.getArabicName(),
                "shortCurNameAR", c.getArabicShortName()
        );
    }

    @Cacheable(cacheNames = "currencyByIso", key = "#isoCode?.toUpperCase()")
    public Map<String, Object> cachedByIso(String isoCode) {
        return findByIsoCode(isoCode);
    }

    public Map<String, Map<String, Object>> findAllByIsoCodes(java.util.Collection<String> isoCodes) {
        if (isoCodes == null || isoCodes.isEmpty()) return java.util.Map.of();
        java.util.List<Currency> rows = currencyRepository.findByIsoCodeInIgnoreCaseAndStatus(isoCodes, "ENABLED");
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
    }

    public Map<String, Map<String, Object>> findAllEnabled() {
        java.util.List<Currency> rows = currencyRepository.findByStatus("ENABLED");
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
    }
}


