package com.example.api.repository;

import com.example.api.entity.CustomerNickname;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerNicknameRepository extends JpaRepository<CustomerNickname, Long> {
    Optional<CustomerNickname> findByAccountNumber(String accountNumber);
    void deleteByAccountNumber(String accountNumber);
    
    /**
     * Find customer nickname by account number and customer ID
     * @param accountNumber the account number
     * @param customerId the customer ID
     * @return Optional CustomerNickname if found
     */
    Optional<CustomerNickname> findByAccountNumberAndCustomerId(String accountNumber, Long customerId);
}