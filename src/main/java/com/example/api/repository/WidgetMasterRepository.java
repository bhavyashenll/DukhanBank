package com.example.api.repository;

import com.example.api.entity.WidgetMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WidgetMasterRepository extends JpaRepository<WidgetMaster, Long> {

    /**
     * Find all widgets by status
     * @param status the status to filter by ('Y' for active, 'N' for inactive)
     * @return list of widgets with the specified status
     */
    List<WidgetMaster> findByStatus(String status);
}


