package com.example.api.service.impl;


import com.example.api.dto.ApiResponse;
import com.example.api.dto.RuleDto;
import com.example.api.dto.UsernameSuggestionRequest;
import com.example.api.dto.UsernameSuggestionResponse;
import com.example.api.infrastructure.helper.UsernameValidator;
import com.example.api.repository.UserRepository;
import com.example.api.service.UsernameRuleService;
import com.example.api.service.UsernameSuggestionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
@Service
@ConditionalOnProperty(prefix = "mock", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class UsernameSuggestionMockServiceImpl implements UsernameSuggestionService {

    private final UsernameRuleService usernameRuleService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;

    @Value("classpath:MockResponses/usermock-data.json")
    private Resource usersJson;

    @Override
    public ApiResponse<UsernameSuggestionResponse> generateUsernames(UsernameSuggestionRequest request) {
        log.info("[MockService] Received username suggestion request.");

        String lang = Optional.ofNullable(request.getRequestInfo())
                .map(UsernameSuggestionRequest.RequestInfo::getLang)
                .orElse("en");
        String qid = Optional.ofNullable(request.getRequestInfo())
                .map(UsernameSuggestionRequest.RequestInfo::getQid)
                .orElse(null);

        log.info("Language selected: {}", lang);
        log.info("QID received: {}", qid);

        // Read mock users from JSON
        List<Map<String, Object>> users = readUsersFromJson();
        if (users.isEmpty()) {
            log.warn("⚠️ No mock users found in JSON file: {}", usersJson.getFilename());
            return ApiResponse.badRequest();
        }

        // Filter by QID
        Map<String, Object> user = users.stream()
                .filter(u -> qid != null && qid.equalsIgnoreCase(String.valueOf(u.get("key"))))
                .findFirst()
                .orElse(null);

        if (user == null) {
            log.warn("No user found for QID: {}", qid);
            return ApiResponse.badRequest();
        }

        // Extract user info
        String first = Optional.ofNullable(user.get("firstname")).map(Object::toString).orElse("user");
        String last = Optional.ofNullable(user.get("lastname")).map(Object::toString).orElse("");
        String dob = Optional.ofNullable(user.get("date_of_birth")).map(Object::toString).orElse("01-01-00");

        log.debug("User Data for QID {} → firstName={}, lastName={}, dateOfBirth={}", qid, first, last, dob);

        // Extract day + month from DOB
        String dayMonth = "0101";
        try {
            String[] parts = dob.split("-");
            if (parts.length >= 2) {
                dayMonth = parts[0] + parts[1]; // ddMM
            }
        } catch (Exception e) {
            log.warn("Failed to parse date_of_birth '{}', using default 0101", dob);
        }

        // Generate username candidates with both first + last name
        List<String> candidates = generateAvailableUsernames(first, last, dayMonth);
        log.info("Generated {} username candidates: {}", candidates.size(), candidates);

        // Fetch and apply DB rules
        List<RuleDto> rules;
        try {
            rules = usernameRuleService.getRules("username", lang);
            log.info("Retrieved {} username validation rules from DB.", rules.size());
        } catch (RuntimeException e) {
            log.warn("No username validation rules found in DB for type=username, lang={}. Proceeding without validation: {}", lang, e.getMessage());
            rules = Collections.emptyList(); // Proceed without rules validation
        }

        // Validate candidates (if rules exist, otherwise all candidates are considered valid)
        List<String> valid = rules.isEmpty() ? candidates : UsernameValidator.applyUsernameRules(candidates, rules);
        log.info("{} valid usernames after applying rules.", valid.size());

        if (valid.isEmpty()) {
            log.warn("No valid usernames found as per rules.");
            return ApiResponse.badRequest();
        }

        log.info("Returning {} valid username suggestions to client.", valid.size());
        
        // Create UsernameSuggestionResponse with the first 3 usernames
        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        if (valid.size() >= 1) {
            response.setUsername1(valid.get(0));
        }
        if (valid.size() >= 2) {
            response.setUsername2(valid.get(1));
        }
        if (valid.size() >= 3) {
            response.setUsername3(valid.get(2));
        }
        
        return ApiResponse.success(List.of(response));
    }

    private List<Map<String, Object>> readUsersFromJson() {
        try (InputStream is = usersJson.getInputStream()) {
            log.info(" Reading mock user data from file: {}", usersJson.getFilename());
            return objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error(" Failed to read users.json: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    private List<String> generateAvailableUsernames(String baseFirst, String baseLast, String dayMonth) {
        log.debug("Generating unique usernames based on base: {} {}", baseFirst, baseLast);
        List<String> available = new ArrayList<>();

        // build combined base with both first and last name
        String base = (baseFirst + baseLast).replaceAll("[^a-zA-Z]", "").toLowerCase();

        // ensure both first and last contribute
        if (baseFirst.length() >= 3 && baseLast.length() >= 2) {
            base = baseFirst.substring(0, Math.min(4, baseFirst.length())) +
                    baseLast.substring(0, Math.min(3, baseLast.length()));
        }

        base = base + dayMonth;
        log.info(" Combined base username: {}", base);

        long startTime = System.currentTimeMillis() / 1000;

        for (int i = 0; i < 20 && available.size() < 3; i++) {
            long gap = startTime + (i * 37); // adds spacing to avoid adjacent numbers
            String suffix = (i == 0) ? "" : String.valueOf(gap % 10000);

            String candidate = base + suffix;
            boolean exists = userRepository.existsByUserIdIgnoreCase(candidate);
            log.debug("🔹 Candidate '{}' | Exists in DB: {}", candidate, exists);

            if (!exists && !available.contains(candidate)) {
                available.add(candidate);
                log.debug(" Added candidate: {}", candidate);
            }
        }

        log.info("Final list of available usernames: {}", available);
        return available;
    }
}