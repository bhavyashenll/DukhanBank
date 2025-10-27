package com.example.api.repository;


import com.example.api.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {
    List<RuleEntity> findByTypeAndLanguage(String type, String language);
}