package com.example.api.repository;

import com.example.api.entity.ProfitRateLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfitRateLabelRepository extends JpaRepository<ProfitRateLabel, Long> {

    /**
     * Find profit rate label by rate code and active status
     */
    Optional<ProfitRateLabel> findByRateCodeIgnoreCaseAndIsActive(String rateCode, Boolean isActive);

    /**
     * Find all active profit rate labels
     */
    List<ProfitRateLabel> findByIsActive(Boolean isActive);

    /**
     * Find profit rate labels by rate codes and active status
     */
    List<ProfitRateLabel> findByRateCodeInIgnoreCaseAndIsActive(List<String> rateCodes, Boolean isActive);

    /**
     * Find profit rate label by rate code (regardless of active status)
     */
    Optional<ProfitRateLabel> findByRateCodeIgnoreCase(String rateCode);

    /**
     * Find profit rate labels by English label containing text (case insensitive)
     */
    @Query("SELECT prl FROM ProfitRateLabel prl WHERE LOWER(prl.englishLabel) LIKE LOWER(CONCAT('%', :searchText, '%')) AND prl.isActive = :isActive")
    List<ProfitRateLabel> findByEnglishLabelContainingIgnoreCaseAndIsActive(@Param("searchText") String searchText, @Param("isActive") Boolean isActive);

    /**
     * Find profit rate labels by Arabic label containing text (case insensitive)
     */
    @Query("SELECT prl FROM ProfitRateLabel prl WHERE LOWER(prl.arabicLabel) LIKE LOWER(CONCAT('%', :searchText, '%')) AND prl.isActive = :isActive")
    List<ProfitRateLabel> findByArabicLabelContainingIgnoreCaseAndIsActive(@Param("searchText") String searchText, @Param("isActive") Boolean isActive);
}
