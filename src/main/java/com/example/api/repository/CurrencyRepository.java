package com.example.api.repository;

import com.example.api.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    
    Optional<Currency> findByIsoCodeIgnoreCaseAndStatus(String isoCode, String status);
    
    List<Currency> findByIsoCodeInIgnoreCaseAndStatus(java.util.Collection<String> isoCodes, String status);
    
    List<Currency> findByStatus(String status);
}
