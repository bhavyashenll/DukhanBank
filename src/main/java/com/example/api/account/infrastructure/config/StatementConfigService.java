package com.example.api.account.infrastructure.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatementConfigService {
    
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    
    @Value("${statement.config.header.retail}")
    private String retailHeaderConfig;
    
    @Value("${statement.config.header.private}")
    private String privateHeaderConfig;
    
    @Value("${statement.config.table.retail}")
    private String retailTableConfig;
    
    @Value("${statement.config.table.private}")
    private String privateTableConfig;
    
    public StatementConfigService(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }
    
    public List<Map<String, Object>> getHeaderFields(String segmentName) throws IOException {
        String configPath = segmentName.equalsIgnoreCase("RETAIL") ? retailHeaderConfig : privateHeaderConfig;
        Resource resource = resourceLoader.getResource("classpath:" + configPath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        }
    }
    
    public List<Map<String, Object>> getTableColumns(String segmentName) throws IOException {
        String configPath = segmentName.equalsIgnoreCase("RETAIL") ? retailTableConfig : privateTableConfig;
        Resource resource = resourceLoader.getResource("classpath:" + configPath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        }
    }
}

