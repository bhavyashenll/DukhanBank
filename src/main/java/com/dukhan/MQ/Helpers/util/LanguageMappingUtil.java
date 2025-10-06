package com.dukhan.MQ.Helpers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageMappingUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(LanguageMappingUtil.class);
    
    /**
     * Maps language from language header to requestorLanguage format
     * EN -> E, AR -> A
     * @param language the language value from request header
     * @return mapped requestorLanguage value
     */
    public static String mapLanguageToRequestorLanguage(String language) {
        if (language == null) {
            return "E"; // default to English
        }
        
        String languageUpper = language.trim().toUpperCase();
        switch (languageUpper) {
            case "EN":
                return "E";
            case "AR":
                return "A";
            default:
                logger.warn("Unknown language '{}', defaulting to 'E'", language);
                return "E";
        }
    }
}
