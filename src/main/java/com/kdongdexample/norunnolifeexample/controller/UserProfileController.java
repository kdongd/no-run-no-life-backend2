package com.kdongdexample.norunnolifeexample.controller;

import com.kdongdexample.norunnolifeexample.domain.UserProfile;
import com.kdongdexample.norunnolifeexample.dto.UserProfileRequest;
import com.kdongdexample.norunnolifeexample.dto.UserProfileResponse;
import com.kdongdexample.norunnolifeexample.exception.ErrorResponse;
import com.kdongdexample.norunnolifeexample.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "UserProfile", description = "회원 프로필(AI 코칭용) 관리 API")
@RestController
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Operation(summary = "프로필 작성", description = "회원가입 완료 후 AI 코칭에 쓰일 신체/목표 정보를 최초 1회 등록한다")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 프로필이 존재함",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/users/me/profile")
    public ResponseEntity<UserProfileResponse> createProfile(@Valid @RequestBody UserProfileRequest request,
                                                             @AuthenticationPrincipal Long userId) {
        UserProfile profile = userProfileService.createProfile(userId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(UserProfileResponse.from(profile));
    }
}
