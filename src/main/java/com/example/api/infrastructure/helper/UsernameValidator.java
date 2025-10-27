package com.example.api.infrastructure.helper;


import com.example.api.dto.RuleDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
public class UsernameValidator {

    public static List<String> applyUsernameRules(List<String> usernames, List<RuleDto> rules) {
        List<String> valid = new ArrayList<>();
        log.info("Applying validation rules to {} generated usernames...", usernames.size());

        for (String username : usernames) {
            boolean allPass = true;
            log.debug("Validating username: '{}'", username);

            for (RuleDto rule : rules) {
                String raw = Optional.ofNullable(rule.getPattern()).orElse("").trim();
                if (raw.isEmpty()) continue;

                // Convert DB double-escaped slashes to single (e.g., "^\\\\S+$" -> "^\S+$")
                String pattern = raw.replace("\\\\", "\\");

                boolean matches = true;
                try {
                    if (pattern.startsWith("length>=")) {
                        int min = Integer.parseInt(pattern.substring(8));
                        matches = username.length() >= min;
                    } else if (pattern.startsWith("length<=")) {
                        int max = Integer.parseInt(pattern.substring(8));
                        matches = username.length() <= max;
                    } else {
                        // regex match
                        matches = Pattern.matches(pattern, username);
                    }
                } catch (Exception e) {
                    log.error("Error applying rule '{}' to username '{}': {}", pattern, username, e.getMessage());
                    matches = false;
                }

                log.debug("Rule '{}' applied to '{}': match={}", pattern, username, matches);

                if (!matches) {
                    allPass = false;
                    log.debug("Username '{}' failed rule '{}'", username, pattern);
                    break;
                }
            }

            if (allPass) {
                valid.add(username);
            }
        }

        log.info("Valid usernames after applying rules: {}", valid);
        return valid;
    }
}
