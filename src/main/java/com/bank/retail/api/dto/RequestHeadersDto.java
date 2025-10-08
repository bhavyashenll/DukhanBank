package com.bank.retail.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestHeadersDto {
    private String serviceId;
    private String channel;
    private String lang;
    private String unit;
    private String ipAddress;
    private String deviceId;
    private String username;
    private String userId;
    private String partnerId;
    private String correlationId;
    private String authType;
}
