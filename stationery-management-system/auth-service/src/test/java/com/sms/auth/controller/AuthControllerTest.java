package com.sms.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.auth.model.User;
import com.sms.auth.model.dto.AuthResponse;
import com.sms.auth.model.dto.LoginRequest;
import com.sms.auth.model.dto.RegisterRequest;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.security.JwtUtil;
import com.sms.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(JwtUtil.class)
@TestPropertySource(properties = {
        "jwt.secret=verysecretkeyforjwtsmsapp12345678901234567890",
        "jwt.expiration=3600000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@sms.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setRole(User.Role.STUDENT);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@sms.com");
        loginRequest.setPassword("password123");

        authResponse = AuthResponse.builder()
                .token("mockJwtToken")
                .id(1L)
                .email("test@sms.com")
                .fullName("Test User")
                .role(User.Role.STUDENT)
                .build();

        user = User.builder()
                .id(1L)
                .email("test@sms.com")
                .fullName("Test User")
                .role(User.Role.STUDENT)
                .build();
    }

    @Test
    void register_Returns201_WhenValidRequest() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@sms.com"))
                .andExpect(jsonPath("$.token").value("mockJwtToken"));
    }

    @Test
    void register_Returns400_WhenInvalidEmail() throws Exception {
        registerRequest.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Returns200_WhenValidCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@sms.com"))
                .andExpect(jsonPath("$.token").value("mockJwtToken"));
    }

    @Test
    void getCurrentUser_Returns200_WhenValidToken() throws Exception {
        String token = jwtUtil.generateToken(user);
        when(userRepository.findByEmail("test@sms.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@sms.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void getCurrentUser_Returns401_WhenInvalidTokenFormat() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}
