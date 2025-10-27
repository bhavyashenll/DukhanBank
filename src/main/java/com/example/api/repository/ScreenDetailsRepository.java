package com.example.api.repository;

import com.example.api.entity.ScreenDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScreenDetailsRepository extends JpaRepository<ScreenDetails, Long> {
    
    Optional<ScreenDetails> findByScreenNameIgnoreCaseAndIsActive(String screenName, Boolean isActive);
}
