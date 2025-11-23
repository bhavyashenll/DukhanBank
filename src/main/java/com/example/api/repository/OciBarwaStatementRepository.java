package com.example.api.repository;

import com.example.api.entity.OciBarwaStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OciBarwaStatementRepository extends JpaRepository<OciBarwaStatement, Long> {

    /**
     * Find statement by customer number, source number, statement type, month and year.
     * Matches the unique index on customer_number, source_number, and month.
     */
    @Query("SELECT o FROM OciBarwaStatement o " +
            "WHERE o.customerNumber = :customerId " +
            "AND o.sourceNumber = :accountNumber " +
            "AND o.statementType = :statementType " +
            "AND YEAR(o.statementDate) = :year " +
            "AND MONTH(o.statementDate) = :month")
    Optional<OciBarwaStatement> findStatement(
            @Param("customerId") String customerId,
            @Param("accountNumber") String accountNumber,
            @Param("statementType") String statementType,
            @Param("month") Integer month,
            @Param("year") Integer year
    );
}

