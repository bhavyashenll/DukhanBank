package com.example.api.service.impl;

import com.example.api.dto.QuickLinksResponse;
import com.example.api.dto.WidgetsResponse;
import com.example.api.entity.QuickLinks;
import com.example.api.entity.SegmentQuicklinkMap;
import com.example.api.entity.Segments;
import com.example.api.entity.WidgetMaster;
import com.example.api.exception.CustomApiException;
import com.example.api.repository.SegmentQuicklinkMapRepository;
import com.example.api.repository.SegmentsRepository;
import com.example.api.repository.WidgetMasterRepository;
import com.example.api.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private SegmentsRepository segmentsRepository;

    @Autowired
    private SegmentQuicklinkMapRepository segmentQuicklinkMapRepository;

    @Autowired
    private WidgetMasterRepository widgetMasterRepository;

    @Override
    @Transactional(readOnly = true)
    public WidgetsResponse getActiveWidgets(String language) {
        logger.info("Fetching all active widgets with language: {}", language);

        // Fetch all active widgets (status = 'Y')
        List<WidgetMaster> activeWidgets = widgetMasterRepository.findByStatus("Y");

        if (activeWidgets == null || activeWidgets.isEmpty()) {
            logger.info("No active widgets found");
            return WidgetsResponse.builder()
                    .widgets(List.of())
                    .build();
        }

        // Determine language preference (default to English)
        boolean useArabic = language != null && "ar".equalsIgnoreCase(language.toLowerCase());

        // Convert to DTO with language-specific name
        List<WidgetsResponse.WidgetDTO> widgetDTOs = activeWidgets.stream()
                .map(w -> {
                    String widgetName = useArabic ? w.getWidgetNameAr() : w.getWidgetNameEn();
                    // Fallback to English if Arabic name is not available
                    if (useArabic && (widgetName == null || widgetName.isBlank())) {
                        widgetName = w.getWidgetNameEn();
                    }
                    return WidgetsResponse.WidgetDTO.builder()
                            .widgetId(w.getWidgetId())
                            .widgetName(widgetName)
                            .build();
                })
                .collect(Collectors.toList());

        logger.info("Found {} active widgets", widgetDTOs.size());

        return WidgetsResponse.builder()
                .widgets(widgetDTOs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuickLinksResponse getQuickLinksBySegmentId(Long segmentId, String language) {
        if (segmentId == null) {
            logger.warn("Segment ID is null");
            throw new CustomApiException("000400", "Segment ID cannot be null", HttpStatus.BAD_REQUEST);
        }

        logger.info("Fetching quick links for segment ID: {}", segmentId);

        // Validate that the segment exists
        Segments segment = segmentsRepository.findById(segmentId)
                .orElseThrow(() -> new CustomApiException("000404", "Segment not found for segment ID: " + segmentId, HttpStatus.NOT_FOUND));

        logger.info("Segment found: ID={}, NameEn={}, NameAr={}", segmentId, segment.getNameEn(), segment.getNameAr());

        // Get quicklink mappings for this segment ID
        List<SegmentQuicklinkMap> segmentQuicklinkMappings =
                segmentQuicklinkMapRepository.findActiveQuickLinksBySegmentId(segmentId);

        if (segmentQuicklinkMappings == null || segmentQuicklinkMappings.isEmpty()) {
            logger.warn("No quick links mapped to segment ID: {}", segmentId);
            return QuickLinksResponse.builder()
                    .quickLinks(List.of())
                    .build();
        }

        // Extract active QuickLinks from mappings (already filtered by query)
        List<QuickLinks> quickLinks = segmentQuicklinkMappings.stream()
                .map(SegmentQuicklinkMap::getQuickLink)
                .filter(ql -> ql != null && "Y".equals(ql.getIsActive())) // Additional safeguard
                .distinct()
                .collect(Collectors.toList());

        logger.info("Found {} active quick links for segment ID: {}", quickLinks.size(), segmentId);

        // Determine language preference (default to English)
        boolean useArabic = language != null && "ar".equalsIgnoreCase(language.toLowerCase());

        // Convert to DTO with language-specific name
        List<QuickLinksResponse.QuickLinkDTO> quickLinkDTOs = quickLinks.stream()
                .map(ql -> {
                    String quicklinkName = useArabic ? ql.getNameAr() : ql.getNameEn();
                    // Fallback to English if Arabic name is not available
                    if (useArabic && (quicklinkName == null || quicklinkName.isBlank())) {
                        quicklinkName = ql.getNameEn();
                    }
                    return QuickLinksResponse.QuickLinkDTO.builder()
                            .quicklinkId(ql.getId())
                            .quicklinkName(quicklinkName)
                            .build();
                })
                .collect(Collectors.toList());

        return QuickLinksResponse.builder()
                .quickLinks(quickLinkDTOs)
                .build();
    }
}