package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;

import java.time.LocalDateTime;

public record WorkoutSummaryResponse(
        Long id,
        WorkoutType type,
        Integer durationMinutes,
        String memo,
        LocalDateTime workoutDateTime
) {
    public static WorkoutSummaryResponse from(Workout workout) {
        return new WorkoutSummaryResponse(
                workout.getId(),
                workout.getType(),
                workout.getDurationMinutes(),
                workout.getMemo(),
                workout.getWorkoutDateTime()
        );
    }
}