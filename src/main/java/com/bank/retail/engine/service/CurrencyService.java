package com.bank.retail.engine.service;

import java.util.Collection;
import java.util.Map;

public interface CurrencyService {
    Map<String, Object> findByIsoCode(String isoCode);
    Map<String, Object> cachedByIso(String isoCode);
    Map<String, Map<String, Object>> findAllByIsoCodes(Collection<String> isoCodes);
    Map<String, Map<String, Object>> findAllEnabled();
}
