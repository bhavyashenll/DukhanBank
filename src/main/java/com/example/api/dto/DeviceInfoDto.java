package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfoDto {
    private String deviceId;
    private String deviceModel;
    private String devicePlatform;
    private String osVersion;
    private String appVersion;
    private String ipAddress;
    private String browser;
    private String guid;
    private String endToEndId;
    private String vendorId;
    private String globalId;
    private String auid;
    private String functionalId;
    private String unitId;
    private String preferredUnit;
    private String lang;
    private String channelId;
}
