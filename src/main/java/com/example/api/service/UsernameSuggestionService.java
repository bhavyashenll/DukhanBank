package com.example.api.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.UsernameSuggestionRequest;
import com.example.api.dto.UsernameSuggestionResponse;

public interface UsernameSuggestionService {

    ApiResponse<UsernameSuggestionResponse> generateUsernames(UsernameSuggestionRequest request);
}
