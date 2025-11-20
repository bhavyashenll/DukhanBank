package com.example.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "IP Address is required")
    private String ipAddress;

    @NotBlank(message = "Vendor ID is required")
    private String vendorId;

    @NotBlank(message = "OS Version is required")
    private String osVersion;

    @NotBlank(message = "OS Type is required")
    private String osType;

    @NotBlank(message = "App Version is required")
    private String appVersion;

    @NotBlank(message = "End to End ID is required")
    private String endToEndId;
}