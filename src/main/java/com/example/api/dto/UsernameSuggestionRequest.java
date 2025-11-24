package com.example.api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsernameSuggestionRequest {

    private RequestInfo requestInfo;
    private DeviceInfo deviceInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestInfo {
        private String customerId;
        private String lang;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        private String deviceId;
        private String ipAddress;
        private String vendorId;
        private String osVersion;
        private String osType;
        private String appVersion;
        private String endToEndId;
    }
}
