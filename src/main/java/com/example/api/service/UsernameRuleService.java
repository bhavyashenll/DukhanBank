package com.example.api.service;



import com.example.api.dto.RuleDto;
import com.example.api.entity.RuleEntity;
import com.example.api.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsernameRuleService {

    @Autowired
    private RuleRepository ruleRepository;

    // Retrieve username and password credentials by Language (from DB)
    public List<RuleDto> getRules(String type, String lang) {

        if (!"username".equalsIgnoreCase(type) && !"password".equalsIgnoreCase(type)) {
            throw new RuntimeException("Invalid type parameter. Allowed values: 'username', 'password'");
        }

        String languageCode = extractLanguageCode(lang);

        if (!"en".equalsIgnoreCase(languageCode) && !"ar".equalsIgnoreCase(languageCode)) {
            throw new RuntimeException("Invalid language parameter. Allowed values: 'en', 'ar'");
        }

        List<RuleEntity> rulesFromDb = ruleRepository.findByTypeAndLanguage(type.toLowerCase(), languageCode);

        if (rulesFromDb.isEmpty()) {
            throw new RuntimeException("No rules found for type=" + type + ", lang=" + languageCode);
        }

        return rulesFromDb.stream()
                .map(rule -> new RuleDto(rule.getDescription(), rule.getPattern()))
                .collect(Collectors.toList());
    }


    // Helper method to extract language code from Accept-Language header
    private String extractLanguageCode(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return "en"; // Default to English
        }

        // Handle formats like "en-US", "ar-SA", etc.
        String[] parts = acceptLanguage.split("-");
        String languageCode = parts[0].toLowerCase();

        switch (languageCode) {
            case "en":
                return "en";
            case "ar":
                return "ar";
            default:
                return "en"; // Default fallback
        }
    }
}
