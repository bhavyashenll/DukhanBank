package com.example.api.service;

import com.example.api.entity.ProductAccount;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductAccountsService {
    
    /**
     * Find product account by type, class_code, currency and active status
     * @param type Product type
     * @param classCode Product class code
     * @param currency Currency code
     * @return Optional ProductAccount
     */
    Optional<ProductAccount> findByTypeAndClassCodeAndCurrency(String type, String classCode, String currency);
    
    /**
     * Find multiple product accounts by type, class codes, currencies and active status
     * @param typeClassCodeCurrencyList List of "type:classCode:currency" strings
     * @return Map with key "type:classCode:currency" and value ProductAccount
     */
    Map<String, ProductAccount> findByTypeAndClassCodeAndCurrencyBatch(List<String> typeClassCodeCurrencyList);
}
