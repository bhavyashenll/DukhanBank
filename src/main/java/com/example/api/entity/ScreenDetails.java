package com.example.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "screen_details")
@Data
public class ScreenDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long screenId;
    
    @Column(name = "screen_name")
    private String screenName;
    
    @Column(name = "screen_description")
    private String screenDescription;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created_date")
    private java.time.LocalDateTime createdDate;
    
    @Column(name = "modified_date")
    private java.time.LocalDateTime modifiedDate;
}
