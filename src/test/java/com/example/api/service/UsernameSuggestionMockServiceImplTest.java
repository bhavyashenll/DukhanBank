package com.example.api.service;

import com.example.api.dto.ApiResponse;
import com.example.api.dto.RuleDto;
import com.example.api.dto.UsernameSuggestionRequest;
import com.example.api.dto.UsernameSuggestionResponse;
import com.example.api.repository.UserRepository;
import com.example.api.infrastructure.helper.UsernameValidator;
import com.example.api.service.impl.UsernameSuggestionMockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsernameSuggestionMockServiceImplTest {

    @Mock
    private UsernameRuleService usernameRuleService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Resource usersJson;

    @InjectMocks
    private UsernameSuggestionMockServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);


        service = new UsernameSuggestionMockServiceImpl(usernameRuleService, userRepository);


        ReflectionTestUtils.setField(service, "usersJson", usersJson);
    }


    @Test
    void testGenerateUsernames_Success() throws Exception {

        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);


        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertNotNull(response);
        assertEquals("000000", response.getStatus().getCode());
        assertEquals("Success", response.getStatus().getDescription());
        assertTrue(response.getData().size() == 1); // should return one UsernameSuggestionResponse object
        assertNotNull(response.getData().get(0).getUsername1());
        assertTrue(response.getData().get(0).getUsername1().toLowerCase().contains("john"));

    }

    @Test
    void testGenerateUsernames_NoUsersFound() throws Exception {

        String mockJson = "[]";
        when(usersJson.getInputStream()).thenReturn(new ByteArrayInputStream(mockJson.getBytes()));

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);
        assertEquals("000400", response.getStatus().getCode());
        assertEquals("Bad request", response.getStatus().getDescription());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void testGenerateUsernames_QidNotFound() throws Exception {

        String mockJson = "[{\"key\":\"99999ABC\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        when(usersJson.getInputStream()).thenReturn(new ByteArrayInputStream(mockJson.getBytes()));

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);
        assertEquals("000400", response.getStatus().getCode());
        assertEquals("Bad request", response.getStatus().getDescription());
    }

    @Test
    void testGenerateUsernames_NoValidUsernames() throws Exception {

        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"A\",\"lastname\":\"B\",\"date_of_birth\":\"01-01-90\"}]";
        when(usersJson.getInputStream()).thenReturn(new ByteArrayInputStream(mockJson.getBytes()));


        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 10", "length>=10")));

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo =
                new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        try (MockedStatic<UsernameValidator> validatorMock = mockStatic(UsernameValidator.class)) {

            validatorMock.when(() -> UsernameValidator.applyUsernameRules(anyList(), anyList()))
                    .thenReturn(List.of());

            ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

            assertEquals("000400", response.getStatus().getCode());
            assertTrue(response.getData().isEmpty());
        }
    }



    @Test
    void testGenerateUsernames_ExceptionReadingJson() throws Exception {
        when(usersJson.getInputStream()).thenThrow(new RuntimeException("IO Error"));

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);
        assertEquals("000400", response.getStatus().getCode());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void testGenerateUsernames_InvalidDateFormat() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"invalid-date\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertNotNull(response);
        assertEquals("000000", response.getStatus().getCode());
        assertTrue(response.getData().size() == 1);
        assertNotNull(response.getData().get(0).getUsername1());
    }

    @Test
    void testGenerateUsernames_NullDateOfBirth() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":null}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertNotNull(response);
        assertEquals("000000", response.getStatus().getCode());
        assertTrue(response.getData().size() == 1);
        assertNotNull(response.getData().get(0).getUsername1());
    }

    @Test
    void testGenerateUsernames_ShortNames() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"A\",\"lastname\":\"B\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertNotNull(response);
        assertEquals("000000", response.getStatus().getCode());
        assertTrue(response.getData().size() == 1);
        assertNotNull(response.getData().get(0).getUsername1());
    }

    @Test
    void testGenerateUsernames_SpecialCharactersInNames() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John@#$%\",\"lastname\":\"Doe-123\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertNotNull(response);
        assertEquals("000000", response.getStatus().getCode());
        assertTrue(response.getData().size() == 1);
        assertNotNull(response.getData().get(0).getUsername1());
    }

    @Test
    void testGenerateUsernames_NullNames() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":null,\"lastname\":null,\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertNotNull(response);
        assertEquals("000000", response.getStatus().getCode());
        assertTrue(response.getData().size() == 1);
        assertNotNull(response.getData().get(0).getUsername1());
    }

    @Test
    void testGenerateUsernames_RepositoryThrowsException() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenThrow(new RuntimeException("DB Error"));

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        // The service should throw the exception since it's not handled
        assertThrows(RuntimeException.class, () -> service.generateUsernames(request));
    }

    @Test
    void testGenerateUsernames_RuleServiceThrowsException() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenThrow(new RuntimeException("Rule service error"));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        // The service should throw the exception since it's not handled
        assertThrows(RuntimeException.class, () -> service.generateUsernames(request));
    }

    @Test
    void testGenerateUsernames_EmptyRules() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of());

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        try (MockedStatic<UsernameValidator> validatorMock = mockStatic(UsernameValidator.class)) {
            validatorMock.when(() -> UsernameValidator.applyUsernameRules(anyList(), anyList()))
                    .thenReturn(List.of("john0101", "johndoe0101", "john0101"));

            ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

            assertNotNull(response);
            assertEquals("000000", response.getStatus().getCode());
            assertTrue(response.getData().size() == 1);
            assertNotNull(response.getData().get(0).getUsername1());
        }
    }

    @Test
    void testGenerateUsernames_NullRules() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(null);

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        // The service should throw NullPointerException since it's not handled
        assertThrows(NullPointerException.class, () -> service.generateUsernames(request));
    }

    @Test
    void testGenerateUsernames_AllCandidatesExistInDB() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(true);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        try (MockedStatic<UsernameValidator> validatorMock = mockStatic(UsernameValidator.class)) {
            validatorMock.when(() -> UsernameValidator.applyUsernameRules(anyList(), anyList()))
                    .thenReturn(List.of());

            ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

            assertEquals("000400", response.getStatus().getCode());
            assertTrue(response.getData().isEmpty());
        }
    }

    @Test
    void testGenerateUsernames_OnlyOneValidUsername() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        try (MockedStatic<UsernameValidator> validatorMock = mockStatic(UsernameValidator.class)) {
            validatorMock.when(() -> UsernameValidator.applyUsernameRules(anyList(), anyList()))
                    .thenReturn(List.of("john0101"));

            ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

            assertNotNull(response);
            assertEquals("000000", response.getStatus().getCode());
            assertTrue(response.getData().size() == 1);
            assertNotNull(response.getData().get(0).getUsername1());
            assertNull(response.getData().get(0).getUsername2());
            assertNull(response.getData().get(0).getUsername3());
        }
    }

    @Test
    void testGenerateUsernames_TwoValidUsernames() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        try (MockedStatic<UsernameValidator> validatorMock = mockStatic(UsernameValidator.class)) {
            validatorMock.when(() -> UsernameValidator.applyUsernameRules(anyList(), anyList()))
                    .thenReturn(List.of("john0101", "johndoe0101"));

            ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

            assertNotNull(response);
            assertEquals("000000", response.getStatus().getCode());
            assertTrue(response.getData().size() == 1);
            assertNotNull(response.getData().get(0).getUsername1());
            assertNotNull(response.getData().get(0).getUsername2());
            assertNull(response.getData().get(0).getUsername3());
        }
    }

    @Test
    void testGenerateUsernames_MoreThanThreeValidUsernames() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", "en");
        request.setRequestInfo(requestInfo);

        try (MockedStatic<UsernameValidator> validatorMock = mockStatic(UsernameValidator.class)) {
            validatorMock.when(() -> UsernameValidator.applyUsernameRules(anyList(), anyList()))
                    .thenReturn(List.of("john0101", "johndoe0101", "john0101", "johndoe0101", "john0101"));

            ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

            assertNotNull(response);
            assertEquals("000000", response.getStatus().getCode());
            assertTrue(response.getData().size() == 1);
            assertNotNull(response.getData().get(0).getUsername1());
            assertNotNull(response.getData().get(0).getUsername2());
            assertNotNull(response.getData().get(0).getUsername3());
        }
    }

    @Test
    void testGenerateUsernames_NullRequestInfo() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        request.setRequestInfo(null);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertEquals("000400", response.getStatus().getCode());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void testGenerateUsernames_NullQid() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo(null, "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertEquals("000400", response.getStatus().getCode());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void testGenerateUsernames_EmptyQid() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("", "en");
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertEquals("000400", response.getStatus().getCode());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void testGenerateUsernames_NullLanguage() throws Exception {
        String mockJson = "[{\"key\":\"12345XYZ\",\"firstname\":\"John\",\"lastname\":\"Doe\",\"date_of_birth\":\"01-01-90\"}]";
        InputStream is = new ByteArrayInputStream(mockJson.getBytes());
        when(usersJson.getInputStream()).thenReturn(is);

        when(usernameRuleService.getRules("username", "en"))
                .thenReturn(List.of(new RuleDto("Min length 3", "length>=3")));

        when(userRepository.existsByUserIdIgnoreCase(anyString())).thenReturn(false);

        UsernameSuggestionRequest request = new UsernameSuggestionRequest();
        UsernameSuggestionRequest.RequestInfo requestInfo = new UsernameSuggestionRequest.RequestInfo("12345XYZ", null);
        request.setRequestInfo(requestInfo);

        ApiResponse<UsernameSuggestionResponse> response = service.generateUsernames(request);

        assertNotNull(response);
        assertEquals("000000", response.getStatus().getCode());
        assertTrue(response.getData().size() == 1);
        assertNotNull(response.getData().get(0).getUsername1());
    }
}
