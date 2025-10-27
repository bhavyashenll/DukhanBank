package com.example.api.repository;

import com.example.api.entity.ConfigurationFieldOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigurationFieldOptionsRepository extends JpaRepository<ConfigurationFieldOptions, Long> {
    
    List<ConfigurationFieldOptions> findByConfigurationIdAndIsActive(Long configurationId, Boolean isActive);
}
