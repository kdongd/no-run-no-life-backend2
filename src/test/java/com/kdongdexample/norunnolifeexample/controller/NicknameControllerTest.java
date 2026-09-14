package com.kdongdexample.norunnolifeexample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdongdexample.norunnolifeexample.config.SecurityConfig;
import com.kdongdexample.norunnolifeexample.dto.NicknameAvailabilityResponse;
import com.kdongdexample.norunnolifeexample.dto.NicknameUpdateRequest;
import com.kdongdexample.norunnolifeexample.exception.InvalidNicknameException;
import com.kdongdexample.norunnolifeexample.exception.NicknameAlreadyExistsException;
import com.kdongdexample.norunnolifeexample.security.CustomAuthenticationEntryPoint;
import com.kdongdexample.norunnolifeexample.security.JwtTokenProvider;
import com.kdongdexample.norunnolifeexample.service.NicknameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NicknameController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class, CustomAuthenticationEntryPoint.class})
class NicknameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NicknameService nicknameService;

    private static UsernamePasswordAuthenticationToken authAs(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    // ===== GET /users/nicknames/availability =====

    @Test
    @DisplayName("availability - 인증 없이 호출해도 200을 반환한다")
    void checkAvailability_returns200_withoutAuthentication() throws Exception {
        given(nicknameService.checkAvailability("runner"))
                .willReturn(new NicknameAvailabilityResponse(true, null));

        mockMvc.perform(get("/users/nicknames/availability").param("nickname", "runner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @DisplayName("availability - 사용 불가능한 닉네임이어도 200과 함께 사유를 반환한다")
    void checkAvailability_returns200_withReason_whenUnavailable() throws Exception {
        given(nicknameService.checkAvailability("admin"))
                .willReturn(new NicknameAvailabilityResponse(false, "사용할 수 없는 닉네임입니다"));

        mockMvc.perform(get("/users/nicknames/availability").param("nickname", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.reason").value("사용할 수 없는 닉네임입니다"));
    }

    // ===== PUT /users/me/nickname =====

    @Test
    @DisplayName("nickname 변경 - 인증된 사용자가 유효한 닉네임으로 요청하면 200")
    void changeNickname_returns200_whenValidAndAuthenticated() throws Exception {
        mockMvc.perform(put("/users/me/nickname")
                        .with(authentication(authAs(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NicknameUpdateRequest("runner"))))
                .andExpect(status().isOk());

        verify(nicknameService).changeNickname(1L, "runner");
    }

    @Test
    @DisplayName("nickname 변경 - 형식이 올바르지 않으면 400")
    void changeNickname_returns400_whenFormatInvalid() throws Exception {
        willThrow(new InvalidNicknameException("닉네임은 2~10자의 한글/영문/숫자만 가능합니다"))
                .given(nicknameService).changeNickname(any(), any());

        mockMvc.perform(put("/users/me/nickname")
                        .with(authentication(authAs(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NicknameUpdateRequest("a"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("nickname 변경 - 이미 사용 중인 닉네임이면 409")
    void changeNickname_returns409_whenAlreadyExists() throws Exception {
        willThrow(new NicknameAlreadyExistsException())
                .given(nicknameService).changeNickname(any(), any());

        mockMvc.perform(put("/users/me/nickname")
                        .with(authentication(authAs(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NicknameUpdateRequest("runner"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("nickname 변경 - 인증 없이 요청하면 401")
    void changeNickname_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/users/me/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new NicknameUpdateRequest("runner"))))
                .andExpect(status().isUnauthorized());
    }
}
