package com.example.api.service.impl;

import com.example.api.entity.ProductAccount;
import com.example.api.repository.ProductAccountRepository;
import com.example.api.service.ProductAccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ProductAccountsServiceImpl implements ProductAccountsService {

    private static final Logger logger = LoggerFactory.getLogger(ProductAccountsServiceImpl.class);

    @Autowired
    private ProductAccountRepository productAccountRepository;

    @Override
    public Optional<ProductAccount> findByTypeAndClassCodeAndCurrency(String type, String classCode, String currency) {
        logger.debug("Finding product account for type: {}, classCode: {}, currency: {}", type, classCode, currency);
        
        if (Objects.isNull(type) || Objects.isNull(classCode) || Objects.isNull(currency)) {
            logger.debug("One or more parameters are null, returning empty");
            return Optional.empty();
        }

        try {
            Optional<ProductAccount> result = productAccountRepository.findByTypeAndClassCodeAndCcyAndStatus(
                type, classCode, currency, "ACTIVE");
            
            if (result.isPresent()) {
                logger.debug("Found product account: {}", result.get());
            } else {
                logger.debug("No product account found for type: {}, classCode: {}, currency: {}", type, classCode, currency);
            }
            
            return result;
        } catch (Exception e) {
            logger.warn("Error finding product account for type: {}, classCode: {}, currency: {}", type, classCode, currency, e);
            return Optional.empty();
        }
    }

    @Override
    public Map<String, ProductAccount> findByTypeAndClassCodeAndCurrencyBatch(List<String> typeClassCodeCurrencyList) {
        logger.debug("Finding product accounts in batch for {} items", typeClassCodeCurrencyList.size());
        
        if (Objects.isNull(typeClassCodeCurrencyList) || typeClassCodeCurrencyList.isEmpty()) {
            logger.debug("Empty or null list provided, returning empty map");
            return new HashMap<>();
        }

        try {
            List<ProductAccount> productAccounts = productAccountRepository.findByTypeAndClassCodeAndCurrencyAndStatusBatch(
                typeClassCodeCurrencyList);
            
            // Create a map with key "type:classCode:currency" and value ProductAccount
            Map<String, ProductAccount> resultMap = productAccounts.stream()
                .collect(Collectors.toMap(
                    pa -> pa.getType() + ":" + pa.getClassCode() + ":" + pa.getCcy(),
                    pa -> pa,
                    (existing, replacement) -> existing // Keep first if duplicate keys
                ));
            
            logger.debug("Found {} product accounts out of {} requested", resultMap.size(), typeClassCodeCurrencyList.size());
            return resultMap;
        } catch (Exception e) {
            logger.warn("Error finding product accounts in batch", e);
            return new HashMap<>();
        }
    }
}
