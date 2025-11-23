package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"status", "data"})
public class ApiResponse<T> {
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("data")
    private List<T> data;

    public static <T> ApiResponse<T> success(List<T> data) {
        return new ApiResponse<>(new Status("000000", "Success"), data);
    }

    public static <T> ApiResponse<T> badRequest() {
        return new ApiResponse<>(new Status("000400", "Bad request"), List.of());
    }
    
    public static <T> ApiResponse<T> badRequest(List<T> data) {
        return new ApiResponse<>(new Status("000400", "Bad request"), data);
    }  
    
    public static <T> ApiResponse<T> deviceInfoNotFound() {
        return new ApiResponse<>(new Status("000400", "Device info not found"), List.of());
    }
    
    public static <T> ApiResponse<T> mandatoryHeaderNotFound() {
        return new ApiResponse<>(new Status("000400", "Incorrect or missing request headers"), List.of());
    }
    
    public static <T> ApiResponse<T> invalidData() {
        return new ApiResponse<>(new Status("000400", "Customer/account not found"), List.of());
    }     
    
    public static <T> ApiResponse<T> noDataFound() {
        return new ApiResponse<>(new Status("000404", "No Data Found"), List.of());
    }

    public static <T> ApiResponse<T> requestTimeout() {
        return new ApiResponse<>(new Status("000408 ", "Request Timeout"), List.of());
    } 
    
    public static <T> ApiResponse<T> duplicateRequest() {
        return new ApiResponse<>(new Status("000409", "Dupliate request"), List.of());
    }      
    
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(new Status("000500", "Internal server error"), List.of());
    }

    public static <T> ApiResponse<T> serviceUnavailable() {
        return new ApiResponse<>(new Status("000503", "Service Unavailable"), List.of());
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
