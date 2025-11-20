package com.example.api.service;

import com.example.api.dto.QuickLinksResponse;
import com.example.api.dto.WidgetsResponse;

public interface DashboardService {
    QuickLinksResponse getQuickLinksBySegmentId(Long segmentId, String language);
    WidgetsResponse getActiveWidgets(String language);
}
