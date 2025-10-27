package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationDto {

    private String fieldKey;
    private String fieldName; // based on screen_id and language fetch english or arabic from configurations table
    private String fieldOption;
    private String fieldLength; 
    private String fieldValidations;
    private String fieldType;
    private List<String> fieldList;  // based on language and configurationid (from screenId) from configuration_field_options table
    private int sequence;
    private String errMessage;
    private Boolean requiresProfanityCheck;
}
