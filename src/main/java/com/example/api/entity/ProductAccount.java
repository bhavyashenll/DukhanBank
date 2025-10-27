package com.example.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product_accounts")
@Data
public class ProductAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "class_code")
    private String classCode;
    
    @Column(name = "ccy")
    private String ccy;
    
    @Column(name = "english_description")
    private String englishDescription;
    
    @Column(name = "arabic_description")
    private String arabicDescription;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "min_value")
    private java.math.BigDecimal minValue;
    
    @Column(name = "max_value")
    private java.math.BigDecimal maxValue;
    
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
}
