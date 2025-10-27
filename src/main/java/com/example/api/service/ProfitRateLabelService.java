package com.example.api.service;

import com.example.api.entity.ProfitRateLabel;
import java.util.Map;
import java.util.Optional;

public interface ProfitRateLabelService {
    
    /**
     * Find profit rate label by rate code and active status
     * @param rateCode Rate code
     * @return Optional ProfitRateLabel
     */
    Optional<ProfitRateLabel> findByRateCode(String rateCode);
    
    /**
     * Find all active profit rate labels and return as map for efficient lookup
     * @return Map with key rateCode (uppercase) and value ProfitRateLabel
     */
    Map<String, ProfitRateLabel> findAllActive();
}
