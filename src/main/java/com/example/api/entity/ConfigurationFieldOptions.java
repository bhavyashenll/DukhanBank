package com.example.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "configuration_field_options")
@Data
public class ConfigurationFieldOptions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "configuration_id")
    private Long configurationId;
    
    @Column(name = "english_option_value")
    private String englishOptionValue;
    
    @Column(name = "arabic_option_value")
    private String arabicOptionValue;
    
    @Column(name = "sequence")
    private Integer sequence;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created_date")
    private java.time.LocalDateTime createdDate;
    
    @Column(name = "modified_date")
    private java.time.LocalDateTime modifiedDate;
}
