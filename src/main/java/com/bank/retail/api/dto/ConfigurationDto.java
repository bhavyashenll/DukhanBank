package com.bank.retail.api.dto;

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

    private String fieldName;
    private String fieldOption;
    private String fieldLength;
    private String fieldValidations;
    private String fieldType;
    private List<String> fieldOptions;
    private int sequence;

    public ConfigurationDto(String fieldName, String fieldOption, String fieldLength, String fieldValidations, String fieldType, int sequence) {
        this.fieldName = fieldName;
        this.fieldOption = fieldOption;
        this.fieldLength = fieldLength;
        this.fieldValidations = fieldValidations;
        this.fieldType = fieldType;
        this.fieldOptions = null;
        this.sequence = sequence;
    }
}
