package com.kdongdexample.norunnolifeexample.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank String idToken
) {}
