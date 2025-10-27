package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProfitRateItem {
    
    @JsonProperty("lastMonthDate")
    private String lastMonthDate;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("rate")
    private String rate;
    
    @JsonProperty("rateCreationDate")
    private String rateCreationDate;
    
    @JsonProperty("productType")
    private String productType;
    
    @JsonProperty("productSubtype")
    private String productSubtype;
    
    @JsonProperty("tenure")
    private String tenure;
    
    @JsonProperty("currency")
    private String currency;

    // Internal field to store original product type for grouping (not serialized)
    private String originalProductType;    
}
