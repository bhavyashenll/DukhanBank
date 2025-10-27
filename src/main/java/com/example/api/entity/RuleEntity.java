package com.example.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rules")
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;       // username or password
    private String language;   // en or ar
    private String description;
    private String pattern;

    // Getters and Setters
}