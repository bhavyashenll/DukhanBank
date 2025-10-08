package com.bank.retail.persistence.repository;

import com.bank.retail.persistence.entity.Configuration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    @Query("select c from Configuration c where c.screenId = :screenId and c.configStatus = 'ACTIVE' and c.callbackRequest = false")
    List<Configuration> findProductFields(@Param("screenId") Long screenId);

    @Query("select c from Configuration c where c.screenId = :screenId and c.configStatus = 'ACTIVE' and c.callbackRequest = true")
    List<Configuration> findCallbackFields(@Param("screenId") Long screenId);
}
