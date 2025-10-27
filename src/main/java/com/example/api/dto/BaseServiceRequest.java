package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class BaseServiceRequest {
    
    @JsonProperty("requestInfo")
    private Map<String, Object> requestInfo;
    
    @JsonProperty("deviceInfo")
    private DeviceInfoDto deviceInfo;
    
    public BaseServiceRequest() {}
    
    public BaseServiceRequest(Map<String, Object> requestData, DeviceInfoDto deviceInfo) {
        this.requestInfo = requestData;
        this.deviceInfo = deviceInfo;
    }
    
    public Map<String, Object> getRequestData() {
        return requestInfo;
    }
    
    public void setRequestData(Map<String, Object> requestData) {
        this.requestInfo = requestData;
    }
}
