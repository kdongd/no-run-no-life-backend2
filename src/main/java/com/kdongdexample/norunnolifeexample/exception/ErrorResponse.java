package com.kdongdexample.norunnolifeexample.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "400")
        int status,
        @Schema(description = "에러 메시지", example = "입력값이 올바르지 않습니다")
        String message,
        @Schema(description = "필드별 에러 목록")
        List<FieldError> errors
) {
    public record FieldError(
            @Schema(description = "필드명", example = "durationMinutes")
            String field,
            @Schema(description = "필드 에러 메시지", example = "1 이상이어야 합니다")
            String message
    ) {}
}
