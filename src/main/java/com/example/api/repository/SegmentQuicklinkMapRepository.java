package com.example.api.repository;

import com.example.api.entity.SegmentQuicklinkMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentQuicklinkMapRepository extends JpaRepository<SegmentQuicklinkMap, Long> {
    List<SegmentQuicklinkMap> findBySegmentIdAndIsActive(Long segmentId, String isActive);

    @Query("SELECT sqm FROM SegmentQuicklinkMap sqm " +
            "JOIN FETCH sqm.quickLink ql " +
            "WHERE sqm.segmentId = :segmentId " +
            "AND sqm.isActive = 'Y' " +
            "AND ql.isActive = 'Y'")
    List<SegmentQuicklinkMap> findActiveQuickLinksBySegmentId(@Param("segmentId") Long segmentId);
}