package com.example.api.account.infrastructure.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
public class StatementColorUtil {
    
    @Value("${statement.color.retail.primary:#1E3A8A}")
    private String retailPrimaryColor;
    
    @Value("${statement.color.retail.secondary:#3B82F6}")
    private String retailSecondaryColor;
    
    @Value("${statement.color.private.primary:#4B5563}")
    private String privatePrimaryColor;
    
    @Value("${statement.color.private.secondary:#6B7280}")
    private String privateSecondaryColor;
    
    public Color getPrimaryColor(String segmentName) {
        String hexColor = segmentName.equalsIgnoreCase("RETAIL") ? retailPrimaryColor : privatePrimaryColor;
        return Color.decode(hexColor);
    }
    
    public Color getSecondaryColor(String segmentName) {
        String hexColor = segmentName.equalsIgnoreCase("RETAIL") ? retailSecondaryColor : privateSecondaryColor;
        return Color.decode(hexColor);
    }
    
    public String getPrimaryColorHex(String segmentName) {
        return segmentName.equalsIgnoreCase("RETAIL") ? retailPrimaryColor : privatePrimaryColor;
    }
    
    public String getSecondaryColorHex(String segmentName) {
        return segmentName.equalsIgnoreCase("RETAIL") ? retailSecondaryColor : privateSecondaryColor;
    }
}

