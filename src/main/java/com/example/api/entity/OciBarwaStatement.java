package com.example.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "oci_barwa_statement")
public class OciBarwaStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_number", length = 255)
    private String customerNumber;

    @Column(name = "path", length = 255)
    private String path;

    @Column(name = "source_number", length = 255)
    private String sourceNumber;

    @Column(name = "source_type", length = 255)
    private String sourceType;

    @Column(name = "statement_date")
    private LocalDateTime statementDate;

    @Column(name = "statement_type", length = 255)
    private String statementType;
}

