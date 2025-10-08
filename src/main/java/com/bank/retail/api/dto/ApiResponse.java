package com.bank.retail.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("data")
    private List<T> data;
    
    public static <T> ApiResponse<T> success(List<T> data) {
        return new ApiResponse<>(new Status("000000", "SUCCESS"), data);
    }
    
    public static <T> ApiResponse<T> noDataFound() {
        return new ApiResponse<>(new Status("000404", "No Data Found"), List.of());
    }
    
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(new Status("000500", "Internal server error"), List.of());
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("description")
        private String description;
    }
}
