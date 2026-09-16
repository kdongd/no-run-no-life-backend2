package com.kdongdexample.norunnolifeexample.controller;

import com.kdongdexample.norunnolifeexample.dto.NicknameAvailabilityResponse;
import com.kdongdexample.norunnolifeexample.dto.NicknameUpdateRequest;
import com.kdongdexample.norunnolifeexample.service.NicknameService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class NicknameController {

    private final NicknameService nicknameService;

    public NicknameController(NicknameService nicknameService) {
        this.nicknameService = nicknameService;
    }

    @GetMapping("/nicknames/availability")
    public ResponseEntity<NicknameAvailabilityResponse> checkNicknameAvailability(
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(
                nicknameService.checkAvailability(nickname)
        );
    }

    @PutMapping("/me/nickname")
    public ResponseEntity<Void> changeNickname(
            @Valid @RequestBody NicknameUpdateRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        nicknameService.changeNickname(userId, request.nickname());

        return ResponseEntity.ok().build();
    }
}
