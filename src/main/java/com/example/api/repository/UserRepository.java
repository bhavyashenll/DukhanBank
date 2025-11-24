package com.example.api.repository;


import com.example.api.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUserIdIgnoreCase(String userId);
    Optional<UserEntity> findByCustomerId(Long customerId);
}

