package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;

import java.time.LocalDateTime;
import java.util.List;

public record WorkoutResponse(
        long id,
        WorkoutType type,
        Integer durationMinutes,
        String memo,
        LocalDateTime workoutDateTime,
        List<WorkoutDetailResponse> details
) {
    public static WorkoutResponse from(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getType(),
                workout.getDurationMinutes(),
                workout.getMemo(),
                workout.getWorkoutDateTime(),
                workout.getDetails().stream()
                        .map(WorkoutDetailResponse::from)
                        .toList()
        );
    }
}