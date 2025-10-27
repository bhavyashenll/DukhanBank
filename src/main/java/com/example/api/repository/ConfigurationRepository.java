package com.example.api.repository;

import com.example.api.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;


@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    /**
     * Find configurations by screen ID and active status, ordered by sequence
     */
    List<Configuration> findByScreenIdAndIsActiveOrderBySequence(Long screenId, Boolean isActive);

    /**
     * Find configuration by field key and screen ID
     */
    Optional<Configuration> findByFieldKeyAndScreenIdAndIsActive(String fieldKey, Long screenId, Boolean isActive);

    /**
     * Find all active configurations
     */
    List<Configuration> findByIsActiveTrue();

    /**
     * Find configurations that require profanity checking
     */
    @Query("SELECT c FROM Configuration c WHERE c.requiresProfanityCheck = true AND c.isActive = true")
    List<Configuration> findConfigurationsRequiringProfanityCheck();
}
