package com.example.api.repository;

import com.example.api.entity.Segments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SegmentsRepository extends JpaRepository<Segments, Long> {
    Optional<Segments> findByNameEn(String nameEn);
    List<Segments> findByIsActive(String isActive);
}