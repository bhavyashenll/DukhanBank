package com.example.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.ZonedDateTime;

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

    @Column(name = "screen_id")
    private Long screenId;

    @Column(name = "field_key")
    private String fieldKey;

    @Column(name = "english_field_name")
    private String englishFieldName;

    @Column(name = "arabic_field_name")
    private String arabicFieldName;

    @Column(name = "field_option")
    private String fieldOption; // mandatory, optional

    @Column(name = "field_length")
    private String fieldLength;

    @Column(name = "field_validations")
    private String fieldValidations; // regex patterns

    @Column(name = "field_type")
    private String fieldType;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "callback_request", nullable = false)
    private Boolean callbackRequest;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "err_message", length = 255)
    private String errMessage;

    @Column(name = "requires_profanity_check")
    private Boolean requiresProfanityCheck;

    // Constructors
    public Configuration(Long id, Long screenId, String fieldKey, String englishFieldName,
            String fieldOption, String fieldLength, String fieldValidations,
            String fieldType, Integer sequence, Boolean isActive) {
        this.id = id;
        this.screenId = screenId;
        this.fieldKey = fieldKey;
        this.englishFieldName = englishFieldName;
        this.fieldOption = fieldOption;
        this.fieldLength = fieldLength;
        this.fieldValidations = fieldValidations;
        this.fieldType = fieldType;
        this.sequence = sequence;
        this.isActive = isActive;
    }
}
