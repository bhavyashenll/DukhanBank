package com.example.api.service.impl;

import com.example.api.entity.ProfitRateLabel;
import com.example.api.repository.ProfitRateLabelRepository;
import com.example.api.service.ProfitRateLabelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProfitRateLabelServiceImpl implements ProfitRateLabelService {

    private static final Logger logger = LoggerFactory.getLogger(ProfitRateLabelServiceImpl.class);

    @Autowired
    private ProfitRateLabelRepository profitRateLabelRepository;

    @Override
    public Optional<ProfitRateLabel> findByRateCode(String rateCode) {
        logger.info("Finding profit rate label for rate code: {}", rateCode);
        
        if (Objects.isNull(rateCode) || rateCode.trim().isEmpty()) {
            logger.info("Rate code is null or empty, returning empty");
            return Optional.empty();
        }

        try {
            Optional<ProfitRateLabel> result = profitRateLabelRepository.findByRateCodeIgnoreCaseAndIsActive(
                rateCode.trim(), true);
            
            if (result.isPresent()) {
                logger.info("Found profit rate label: {}", result.get());
            } else {
                logger.info("No profit rate label found for rate code: {}", rateCode);
            }
            
            return result;
        } catch (Exception e) {
            logger.warn("Error finding profit rate label for rate code: {}", rateCode, e);
            return Optional.empty();
        }
    }

    @Override
    @Cacheable(cacheNames = "profitRateLabels", key = "'allActive'")
    public Map<String, ProfitRateLabel> findAllActive() {
        logger.info("Finding all active profit rate labels");
        
        try {
            java.util.List<ProfitRateLabel> profitRateLabels = profitRateLabelRepository.findByIsActive(true);
            logger.info("Found {} active profit rate labels", profitRateLabels.size());
            
            // Create a map with key rateCode (uppercase) and value ProfitRateLabel
            Map<String, ProfitRateLabel> resultMap = profitRateLabels.stream()
                .collect(Collectors.toMap(
                    prl -> prl.getRateCode().toUpperCase(),
                    prl -> prl,
                    (existing, replacement) -> existing // Keep first if duplicate keys
                ));
            
            logger.info("Successfully mapped {} profit rate labels", resultMap.size());
            return resultMap;
        } catch (Exception e) {
            logger.error("Error finding all active profit rate labels", e);
            return new HashMap<>();
        }
    }
}
