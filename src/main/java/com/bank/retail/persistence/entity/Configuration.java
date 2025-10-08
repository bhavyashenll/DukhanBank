package com.bank.retail.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configurations")
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long screenId;

    @Column(nullable = false)
    private String fieldName; // e.g. Name, Email, Mobile

    @Column(nullable = false)
    private String fieldOption; // mandatory / optional

    @Column(nullable = false)
    private String fieldLength; // e.g. 50, 10

    @Column
    private String fieldValidations; // regex rules

    @Column(nullable = false)
    private String fieldType; // Alphabets / alphaNumeric / Numeric / Dropdown / Date

    @ElementCollection
    @CollectionTable(name = "configuration_field_options", joinColumns = @JoinColumn(name = "configuration_id"))
    @Column(name = "option_value")
    private List<String> fieldOptions; // options when fieldType is dropdown/combo

    @Column(nullable = false)
    private boolean callbackRequest; // true = for callback form

    @Column(nullable = false)
    private String configStatus = "ACTIVE";

    @Column(updatable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column
    private ZonedDateTime updatedAt = ZonedDateTime.now();

    @Column(nullable = false)
    private int sequence;    
}
