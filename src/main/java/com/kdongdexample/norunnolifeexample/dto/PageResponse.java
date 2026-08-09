package com.kdongdexample.norunnolifeexample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        @Schema(description = "현재 페이지 데이터 목록")
        List<T> content,
        @Schema(description = "현재 페이지 번호(0부터 시작)", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "10")
        int size,
        @Schema(description = "전체 요소 수", example = "42")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
