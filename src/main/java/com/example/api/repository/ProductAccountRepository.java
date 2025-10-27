package com.example.api.repository;

import com.example.api.entity.ProductAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAccountRepository extends JpaRepository<ProductAccount, Long> {
    
    @Query("SELECT pa FROM ProductAccount pa WHERE pa.type = :type AND pa.classCode = :classCode AND pa.ccy = :ccy AND UPPER(pa.status) = UPPER(:status)")
    Optional<ProductAccount> findByTypeAndClassCodeAndCcyAndStatus(@Param("type") String type, @Param("classCode") String classCode, @Param("ccy") String ccy, @Param("status") String status);
    
    @Query("SELECT pa FROM ProductAccount pa WHERE " +
           "CONCAT(pa.type, ':', pa.classCode, ':', pa.ccy) IN :typeClassCodeCurrencyList " +
           "AND UPPER(pa.status) = 'ACTIVE'")
    List<ProductAccount> findByTypeAndClassCodeAndCurrencyAndStatusBatch(@Param("typeClassCodeCurrencyList") List<String> typeClassCodeCurrencyList);
}
