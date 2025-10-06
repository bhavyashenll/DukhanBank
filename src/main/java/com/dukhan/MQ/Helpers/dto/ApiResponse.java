package com.dukhan.MQ.Helpers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ApiResponse<T> {
    
    @JsonProperty("status")
    private Status status;
    
    @JsonProperty("data")
    private List<T> data;
    
    public ApiResponse() {}
    
    public ApiResponse(Status status, List<T> data) {
        this.status = status;
        this.data = data;
    }
    
    public static <T> ApiResponse<T> success(List<T> data) {
        return new ApiResponse<>(new Status("000000", "SUCCESS"), data);
    }
    
    public static <T> ApiResponse<T> noDataFound() {
        return new ApiResponse<>(new Status("000404", "No Data Found"), List.of());
    }
    
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(new Status("000500", "Internal server error"), List.of());
    }
    
    // Getters and Setters
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public List<T> getData() {
        return data;
    }
    
    public void setData(List<T> data) {
        this.data = data;
    }
    
    public static class Status {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("description")
        private String description;
        
        public Status() {}
        
        public Status(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        // Getters and Setters
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
