package com.example.api.infrastructure;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service for detecting profanity in text
 */
@Service
public class ProfanityDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfanityDetectionService.class);
    private static final String PROFANITY_WORDS_FILE = "profanity-words.txt";
    
    private List<String> profanityWords;
    
    /**
     * Loads profanity words from the resource file
     */
    private void loadProfanityWords() {
        if (profanityWords != null) {
            return; // Already loaded
        }
        
        profanityWords = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource(PROFANITY_WORDS_FILE);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // Skip empty lines and comments
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        profanityWords.add(line.toLowerCase());
                    }
                }
            }
            logger.info("Loaded {} profanity words from {}", profanityWords.size(), PROFANITY_WORDS_FILE);
        } catch (IOException e) {
            logger.error("Error loading profanity words from {}", PROFANITY_WORDS_FILE, e);
            profanityWords = new ArrayList<>(); // Initialize empty list on error
        }
    }
    
    /**
     * Checks if the given text contains profanity
     * @param text the text to check
     * @return true if profanity is detected, false otherwise
     */
    public boolean containsProfanity(String text) {
        if (Objects.isNull(text) || text.trim().isEmpty()) {
            return false;
        }
        
        loadProfanityWords();
        
        String normalizedText = normalizeText(text);
        String[] words = normalizedText.split("\\s+");
        
        for (String word : words) {
            if (profanityWords.contains(word.toLowerCase())) {
                logger.debug("Profanity detected: '{}' in text: '{}'", word, text);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Normalizes text for profanity checking
     * Removes special characters and normalizes spacing
     */
    private String normalizeText(String text) {
        if (Objects.isNull(text)) {
            return "";
        }
        
        // Remove special characters except spaces and letters
        String normalized = text.replaceAll("[^a-zA-Z\\s]", " ");
        
        // Normalize multiple spaces to single space
        normalized = normalized.replaceAll("\\s+", " ");
        
        return normalized.trim();
    }
    
    /**
     * Gets the list of profanity words (for testing purposes)
     */
    public List<String> getProfanityWords() {
        loadProfanityWords();
        return new ArrayList<>(profanityWords);
    }
}
