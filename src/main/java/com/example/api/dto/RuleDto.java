package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleDto {
    private String ruleDescription;
    private String pattern;
}
