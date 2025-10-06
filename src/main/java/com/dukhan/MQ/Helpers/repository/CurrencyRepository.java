package com.dukhan.MQ.Helpers.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dukhan.MQ.Helpers.entity.Currency;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByIsoCodeIgnoreCaseAndStatus(String isoCode, String status);
    java.util.List<Currency> findByIsoCodeInIgnoreCaseAndStatus(java.util.Collection<String> isoCodes, String status);
    java.util.List<Currency> findByStatus(String status);
}


