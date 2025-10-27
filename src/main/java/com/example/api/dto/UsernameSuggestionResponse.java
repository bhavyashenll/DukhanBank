package com.example.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsernameSuggestionResponse {
    
    @JsonProperty("username1")
    private String username1;
    
    @JsonProperty("username2")
    private String username2;
    
    @JsonProperty("username3")
    private String username3;
}
