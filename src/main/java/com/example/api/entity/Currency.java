package com.example.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "currency")
@Data
public class Currency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "iso_code")
    private String isoCode;
    
    @Column(name = "iso_number")
    private String isoNumber;
    
    @Column(name = "english_name")
    private String englishName;
    
    @Column(name = "english_short_name")
    private String englishShortName;
    
    @Column(name = "arabic_name")
    private String arabicName;
    
    @Column(name = "arabic_short_name")
    private String arabicShortName;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "created_date")
    private java.time.LocalDateTime createdDate;
    
    @Column(name = "modified_date")
    private java.time.LocalDateTime modifiedDate;
}
