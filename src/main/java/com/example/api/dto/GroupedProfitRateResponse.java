package com.example.api.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupedProfitRateResponse extends HashMap<String, List<ProfitRateItem>> {
    
    public GroupedProfitRateResponse() {
        super();
    }
    
    public GroupedProfitRateResponse(Map<String, List<ProfitRateItem>> map) {
        super(map);
    }
}
