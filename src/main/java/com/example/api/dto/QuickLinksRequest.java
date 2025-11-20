package com.example.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuickLinksRequest {

    @NotNull(message = "Request info is required")
    @Valid
    private QuickLinksRequestInfo requestInfo;

    @NotNull(message = "Device info is required")
    @Valid
    private DeviceInfo deviceInfo;
}
