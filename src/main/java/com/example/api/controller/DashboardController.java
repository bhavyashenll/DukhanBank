package com.example.api.controller;

import com.example.api.common.constraints.AppConstants;
import com.example.api.dto.*;
import com.example.api.service.DashboardService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @PostMapping("/retreive-all-quicklinks")
    public ResponseEntity<BaseResponse<QuickLinksResponse>> getQuickLinks(
            @RequestHeader(name = AppConstants.SERVICEID, required = true) String serviceId,
            @RequestHeader(name = AppConstants.MODULE_ID, required = true) String moduleId,
            @RequestHeader(name = AppConstants.SUB_MODULE_ID, required = true) String subModuleId,
            @RequestHeader(name = AppConstants.SCREENID, required = true) String screenId,
            @RequestHeader(name = AppConstants.UNIT, required = false, defaultValue = "DEFAULT") String unit,
            @RequestHeader(name = AppConstants.CHANNEL, required = true) String channel,
            @RequestHeader(name = AppConstants.ACCEPT_LANGUAGE, required = false, defaultValue = "en") String lang,
            @RequestBody @Valid QuickLinksRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        // Extract segmentId from requestInfo
        Long segmentId = null;
        if (request.getRequestInfo() != null && request.getRequestInfo().getSegmentId() != null) {
            segmentId = request.getRequestInfo().getSegmentId();
        }

        // Log device information
        logger.info("Quick links request - Device Info: DeviceID={}, IP={}, OS={} {}, AppVersion={}, SegmentId: {}",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getDeviceId() : "N/A",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getIpAddress() : "N/A",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getOsType() : "N/A",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getOsVersion() : "N/A",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getAppVersion() : "N/A",
                segmentId != null ? segmentId : "N/A");

        logger.info("Quick links request - Headers: ServiceId={}, Channel={}, Unit={}, ScreenId={}, ModuleId={}, SubModuleId={}",
                serviceId, channel, unit, screenId, moduleId, subModuleId);

        QuickLinksResponse response = dashboardService.getQuickLinksBySegmentId(segmentId, lang);

        return ResponseEntity.ok(BaseResponse.success(response));
    }

    @PostMapping("/retreive-all-widgets")
    public ResponseEntity<BaseResponse<WidgetsResponse>> getActiveWidgets(
            @RequestHeader(name = AppConstants.SERVICEID, required = true) String serviceId,
            @RequestHeader(name = AppConstants.MODULE_ID, required = true) String moduleId,
            @RequestHeader(name = AppConstants.SUB_MODULE_ID, required = true) String subModuleId,
            @RequestHeader(name = AppConstants.SCREENID, required = true) String screenId,
            @RequestHeader(name = AppConstants.UNIT, required = false, defaultValue = "DEFAULT") String unit,
            @RequestHeader(name = AppConstants.CHANNEL, required = true) String channel,
            @RequestHeader(name = AppConstants.ACCEPT_LANGUAGE, required = false, defaultValue = "en") String lang,
            @RequestBody @Valid WidgetsRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        logger.info("Widgets request - Device Info: DeviceID={}, IP={}, OS={} {}, AppVersion={}",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getDeviceId() : "N/A",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getIpAddress() : "N/A",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getOsType() : "N/A",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getOsVersion() : "N/A",
                request.getDeviceInfo() != null ? request.getDeviceInfo().getAppVersion() : "N/A");

        logger.info("Widgets request - Headers: ServiceId={}, Channel={}, Unit={}, ScreenId={}, ModuleId={}, SubModuleId={}, Language={}",
                serviceId, channel, unit, screenId, moduleId, subModuleId, lang);

        WidgetsResponse activeWidgets = dashboardService.getActiveWidgets(lang);
        return ResponseEntity.ok(BaseResponse.success(activeWidgets));
    }
}