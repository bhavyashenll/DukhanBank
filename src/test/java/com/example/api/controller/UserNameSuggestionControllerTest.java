package com.example.api.controller;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.UsernameSuggestionRequest;
import com.example.api.dto.UsernameSuggestionResponse;
import com.example.api.infrastructure.AppConstant;
import com.example.api.service.UsernameSuggestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureWebMvc
class UserNameSuggestionControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @MockBean
    private UsernameSuggestionService usernameSuggestionService;

    private UsernameSuggestionRequest validRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        validRequest = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        info.setLang("en");
        validRequest.setRequestInfo(info);
        
        // Add DeviceInfo to validRequest to pass validation
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        validRequest.setDeviceInfo(deviceInfo);
    }

    @Test
    void testSuggestUsernames_Success() throws Exception {
        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.HEADER_ACCEPT_LANGUAGE, "en")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status.code").value("000000"))
                .andExpect(jsonPath("$.status.description").value("Successfully processed"))
                .andExpect(jsonPath("$.data.username1").value("ashish01"))
                .andExpect(jsonPath("$.data.username2").value("ashish02"))
                .andExpect(jsonPath("$.data.username3").value("ashish03"));
    }

    @Test
    void testSuggestUsernames_BadRequest() throws Exception {
        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.badRequest();
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.HEADER_ACCEPT_LANGUAGE, "en")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status.code").value("000400"))
                .andExpect(jsonPath("$.status.description").value("Bad request"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testSuggestUsernames_MissingHeaders_ShouldFailValidation() throws Exception {

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_NullQidHandledGracefully() throws Exception {
        validRequest.setRequestInfo(null);

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.badRequest();
        when(usernameSuggestionService.generateUsernames(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000400"));
    }

    @Test
    void testSuggestUsernames_MissingChannelHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_MissingServiceIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_MissingScreenIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_MissingModuleIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_MissingSubModuleIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_InvalidJsonBody() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }")
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_EmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("")
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_ServiceThrowsException() throws Exception {
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status.code").value("000500"))
                .andExpect(jsonPath("$.status.description").value("Internal server error"));
    }

    @Test
    void testSuggestUsernames_EmptyResponseData() throws Exception {
        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of());
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testSuggestUsernames_NullResponseData() throws Exception {
        ApiResponse<UsernameSuggestionResponse> mockResponse = new ApiResponse<>();
        mockResponse.setStatus(new ApiResponse.Status("000000", "Success"));
        mockResponse.setData(null);
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testSuggestUsernames_WithLanguageHeader() throws Exception {
        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.HEADER_ACCEPT_LANGUAGE, "ar")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"))
                .andExpect(jsonPath("$.data.username1").value("ashish01"));
    }

    // DeviceInfo validation tests
    @Test
    void testSuggestUsernames_MissingDeviceInfo_ShouldReturnBadRequest() throws Exception {
        UsernameSuggestionRequest requestWithoutDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        info.setLang("en");
        requestWithoutDeviceInfo.setRequestInfo(info);
        // DeviceInfo is null

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.code").value("000400"))
                .andExpect(jsonPath("$.status.description").value("Device info not found"));
    }

    @Test
    void testSuggestUsernames_WithValidDeviceInfo_Success() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        info.setLang("en");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        deviceInfo.setIpAddress("192.168.1.1");
        deviceInfo.setVendorId("VENDOR123");
        deviceInfo.setOsVersion("Android 12");
        deviceInfo.setOsType("Android");
        deviceInfo.setAppVersion("1.0.0");
        deviceInfo.setEndToEndId("E2E123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"))
                .andExpect(jsonPath("$.data.username1").value("ashish01"));
    }

    // Edge cases for QID validation
    @Test
    void testSuggestUsernames_EmptyQid() throws Exception {
        UsernameSuggestionRequest requestWithEmptyQid = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("");
        info.setLang("en");
        requestWithEmptyQid.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithEmptyQid.setDeviceInfo(deviceInfo);

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.badRequest();
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithEmptyQid))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000400"));
    }

    @Test
    void testSuggestUsernames_QidWithSpecialCharacters() throws Exception {
        UsernameSuggestionRequest requestWithSpecialQid = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("123@#$%^&*()");
        info.setLang("en");
        requestWithSpecialQid.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithSpecialQid.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithSpecialQid))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"));
    }

    @Test
    void testSuggestUsernames_VeryLongQid() throws Exception {
        UsernameSuggestionRequest requestWithLongQid = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("123456789012345678901234567890123456789012345678901234567890");
        info.setLang("en");
        requestWithLongQid.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithLongQid.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithLongQid))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"));
    }

    // Additional header validation tests
    @Test
    void testSuggestUsernames_EmptyChannelHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testSuggestUsernames_EmptyServiceIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testSuggestUsernames_EmptyScreenIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testSuggestUsernames_EmptyModuleIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testSuggestUsernames_EmptySubModuleIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, ""))
                .andExpect(status().is5xxServerError());
    }

    // Additional error scenarios
    @Test
    void testSuggestUsernames_ServiceReturnsNoDataFound() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.noDataFound();
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000404"))
                .andExpect(jsonPath("$.status.description").value("No Data Found"));
    }

    @Test
    void testSuggestUsernames_ServiceReturnsServiceUnavailable() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.serviceUnavailable();
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000503"))
                .andExpect(jsonPath("$.status.description").value("Service Unavailable"));
    }

    @Test
    void testSuggestUsernames_ServiceReturnsRequestTimeout() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.requestTimeout();
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000408 "))
                .andExpect(jsonPath("$.status.description").value("Request Timeout"));
    }

    @Test
    void testSuggestUsernames_ServiceReturnsDuplicateRequest() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.duplicateRequest();
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000409"))
                .andExpect(jsonPath("$.status.description").value("Dupliate request"));
    }

    // Test different content types
    @Test
    void testSuggestUsernames_WrongContentType() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.TEXT_PLAIN)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testSuggestUsernames_WrongAcceptHeader() throws Exception {
        // Mock the service to return a valid response to avoid NPE
        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    // Test with different language values
    @Test
    void testSuggestUsernames_WithDifferentLanguages() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        info.setLang("fr");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.HEADER_ACCEPT_LANGUAGE, "fr")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"));
    }

    @Test
    void testSuggestUsernames_WithNullLanguage() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        info.setLang(null);
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"));
    }

    // Test response processing logic
    @Test
    void testSuggestUsernames_ResponseProcessingWithSuccessCode() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = new ApiResponse<>();
        mockResponse.setStatus(new ApiResponse.Status("000000", "Original Success"));
        mockResponse.setData(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"))
                .andExpect(jsonPath("$.status.description").value("Successfully processed"))
                .andExpect(jsonPath("$.data.username1").value("ashish01"));
    }

    @Test
    void testSuggestUsernames_ResponseProcessingWithNonSuccessCode() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        ApiResponse<UsernameSuggestionResponse> mockResponse = new ApiResponse<>();
        mockResponse.setStatus(new ApiResponse.Status("000400", "Bad Request"));
        mockResponse.setData(List.of());
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000400"))
                .andExpect(jsonPath("$.status.description").value("Bad Request"));
    }

    @Test
    void testSuggestUsernames_ResponseProcessingWithNullStatus() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        ApiResponse<UsernameSuggestionResponse> mockResponse = new ApiResponse<>();
        mockResponse.setStatus(null);
        mockResponse.setData(List.of());
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").isEmpty());
    }

    // Test with different channel values
    @Test
    void testSuggestUsernames_WithMobileChannel() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "MOBILE")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"));
    }

    @Test
    void testSuggestUsernames_WithApiChannel() throws Exception {
        UsernameSuggestionRequest requestWithDeviceInfo = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        requestWithDeviceInfo.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithDeviceInfo.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithDeviceInfo))
                        .header(AppConstant.HEADER_CHANNEL, "API")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"));
    }

    // Test malformed JSON scenarios
    @Test
    void testSuggestUsernames_MalformedJsonWithMissingBrace() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"requestInfo\":{\"qid\":\"1234567890\"")
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testSuggestUsernames_MalformedJsonWithInvalidSyntax() throws Exception {
        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"requestInfo\":{\"qid\":\"1234567890\",\"lang\":\"en\"},\"deviceInfo\":{\"deviceId\":\"DEVICE123\"}")
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().is4xxClientError());
    }

    // Test with whitespace-only values
    @Test
    void testSuggestUsernames_QidWithOnlyWhitespace() throws Exception {
        UsernameSuggestionRequest requestWithWhitespaceQid = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("   ");
        info.setLang("en");
        requestWithWhitespaceQid.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        requestWithWhitespaceQid.setDeviceInfo(deviceInfo);

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.badRequest();
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithWhitespaceQid))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000400"));
    }

    // Test with very large request body
    @Test
    void testSuggestUsernames_VeryLargeRequestBody() throws Exception {
        UsernameSuggestionRequest requestWithLargeData = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo info = new UsernameSuggestionRequest.RequestInfo();
        info.setQid("1234567890");
        info.setLang("en");
        requestWithLargeData.setRequestInfo(info);
        
        UsernameSuggestionRequest.DeviceInfo deviceInfo = new UsernameSuggestionRequest.DeviceInfo();
        deviceInfo.setDeviceId("DEVICE123");
        deviceInfo.setIpAddress("192.168.1.1");
        deviceInfo.setVendorId("VENDOR123");
        deviceInfo.setOsVersion("Android 12");
        deviceInfo.setOsType("Android");
        deviceInfo.setAppVersion("1.0.0");
        deviceInfo.setEndToEndId("E2E123");
        requestWithLargeData.setDeviceInfo(deviceInfo);

        UsernameSuggestionResponse response = new UsernameSuggestionResponse();
        response.setUsername1("ashish01");
        response.setUsername2("ashish02");
        response.setUsername3("ashish03");

        ApiResponse<UsernameSuggestionResponse> mockResponse = ApiResponse.success(List.of(response));
        when(usernameSuggestionService.generateUsernames(any(UsernameSuggestionRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/username/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithLargeData))
                        .header(AppConstant.HEADER_CHANNEL, "WEB")
                        .header(AppConstant.SERVICEID, "USR001")
                        .header(AppConstant.SCREEN_ID, "SCR001")
                        .header(AppConstant.MODULE_ID, "MOD001")
                        .header(AppConstant.SUB_MODULE_ID, "SUB001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value("000000"));
    }


}
