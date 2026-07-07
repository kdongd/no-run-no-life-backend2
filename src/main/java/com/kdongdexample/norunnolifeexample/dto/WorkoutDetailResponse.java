package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.WorkoutDetail;

public record WorkoutDetailResponse(
        Long id,
        Integer sequence,
        String label,
        Integer durationSeconds,
        String note
) {
    public static WorkoutDetailResponse from(WorkoutDetail detail) {
        return new WorkoutDetailResponse(
                detail.getId(),
                detail.getSequence(),
                detail.getLabel(),
                detail.getDurationSeconds(),
                detail.getNote()
        );
    }
}