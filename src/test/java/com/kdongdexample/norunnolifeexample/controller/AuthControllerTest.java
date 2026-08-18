package com.kdongdexample.norunnolifeexample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdongdexample.norunnolifeexample.config.SecurityConfig;
import com.kdongdexample.norunnolifeexample.dto.GoogleLoginRequest;
import com.kdongdexample.norunnolifeexample.dto.LoginRequest;
import com.kdongdexample.norunnolifeexample.dto.SignupRequest;
import com.kdongdexample.norunnolifeexample.dto.TokenResponse;
import com.kdongdexample.norunnolifeexample.exception.EmailAlreadyExistsException;
import com.kdongdexample.norunnolifeexample.exception.InvalidCredentialsException;
import com.kdongdexample.norunnolifeexample.exception.InvalidGoogleTokenException;
import com.kdongdexample.norunnolifeexample.security.JwtTokenProvider;
import com.kdongdexample.norunnolifeexample.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("signup - 유효한 요청이면 201")
    void signup_returns201_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest("test@test.com", "password1234"))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("signup - 이미 존재하는 이메일이면 409")
    void signup_returns409_whenEmailAlreadyExists() throws Exception {
        willThrow(new EmailAlreadyExistsException("test@test.com")).given(authService).signup(any());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignupRequest("test@test.com", "password1234"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("login - 올바른 요청이면 200과 토큰을 반환한다")
    void login_returns200WithToken_whenRequestIsValid() throws Exception {
        given(authService.login(any())).willReturn(new TokenResponse("jwt-token", "Bearer"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("test@test.com", "password1234"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("login - 잘못된 자격증명이면 401")
    void login_returns401_whenCredentialsAreInvalid() throws Exception {
        given(authService.login(any())).willThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("test@test.com", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("loginWithGoogle - 유효한 idToken이면 200과 토큰을 반환한다")
    void loginWithGoogle_returns200WithToken_whenIdTokenIsValid() throws Exception {
        given(authService.loginWithGoogle(any())).willReturn(new TokenResponse("jwt-token", "Bearer"));

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("valid-id-token"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("loginWithGoogle - 유효하지 않은 idToken이면 401")
    void loginWithGoogle_returns401_whenIdTokenIsInvalid() throws Exception {
        given(authService.loginWithGoogle(any())).willThrow(new InvalidGoogleTokenException());

        mockMvc.perform(post("/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("invalid-token"))))
                .andExpect(status().isUnauthorized());
    }
}
