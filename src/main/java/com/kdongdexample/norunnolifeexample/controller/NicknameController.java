package com.kdongdexample.norunnolifeexample.controller;

import com.kdongdexample.norunnolifeexample.dto.NicknameAvailabilityResponse;
import com.kdongdexample.norunnolifeexample.dto.NicknameUpdateRequest;
import com.kdongdexample.norunnolifeexample.service.NicknameService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class NicknameController {

    private final NicknameService nicknameService;

    public NicknameController(NicknameService nicknameService) {
        this.nicknameService = nicknameService;
    }

    @GetMapping("/users/nicknames/availability")
    public ResponseEntity<NicknameAvailabilityResponse> checkAvailability(@RequestParam String nickname) {
        return ResponseEntity.ok(nicknameService.checkAvailability(nickname));
    }

    @PutMapping("/users/me/nickname")
    public ResponseEntity<Void> changeNickname(@AuthenticationPrincipal Long userId,
                                               @RequestBody NicknameUpdateRequest request) {
        nicknameService.changeNickname(userId, request.nickname());
        return ResponseEntity.ok().build();
    }
}
