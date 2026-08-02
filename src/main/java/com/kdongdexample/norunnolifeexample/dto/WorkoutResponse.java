package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.TechniqueType;
import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;

import java.time.LocalDateTime;
import java.util.List;

public record WorkoutResponse(
        Long id,
        WorkoutType type,
        Integer durationMinutes,
        String memo,
        LocalDateTime workoutDateTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<WorkoutDetailResponse> details,

        // 러닝 전용
        Double distanceKm,
        String place,
        Integer caloriesBurned,

        // 복싱 전용
        Integer rounds,
        String sparringPartner,
        TechniqueType techniqueType
) {
    public static WorkoutResponse from(Workout workout) {
        WorkoutTypeFields typeFields = WorkoutTypeFields.from(workout);

        return new WorkoutResponse(
                workout.getId(),
                workout.getType(),
                workout.getDurationMinutes(),
                workout.getMemo(),
                workout.getWorkoutDateTime(),
                workout.getCreatedAt(),
                workout.getUpdatedAt(),
                workout.getDetails().stream()
                        .map(WorkoutDetailResponse::from)
                        .toList(),
                typeFields.distanceKm(),
                typeFields.place(),
                typeFields.caloriesBurned(),
                typeFields.rounds(),
                typeFields.sparringPartner(),
                typeFields.techniqueType()
        );
    }
}
