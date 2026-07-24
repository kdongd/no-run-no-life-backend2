package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.BoxingWorkout;
import com.kdongdexample.norunnolifeexample.domain.RunningWorkout;
import com.kdongdexample.norunnolifeexample.domain.TechniqueType;
import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;

import java.time.LocalDateTime;

public record WorkoutSummaryResponse(
        Long id,
        WorkoutType type,
        Integer durationMinutes,
        String memo,
        LocalDateTime workoutDateTime,

        // 러닝 전용
        Double distanceKm,
        String place,
        Integer caloriesBurned,

        // 복싱 전용
        Integer rounds,
        String sparringPartner,
        TechniqueType techniqueType
) {
    public static WorkoutSummaryResponse from(Workout workout) {
        Double distanceKm = null;
        String place = null;
        Integer caloriesBurned = null;
        Integer rounds = null;
        String sparringPartner = null;
        TechniqueType techniqueType = null;

        if (workout instanceof RunningWorkout running) {
            distanceKm = running.getDistanceKm();
            place = running.getPlace();
            caloriesBurned = running.getCaloriesBurned();
        } else if (workout instanceof BoxingWorkout boxing) {
            rounds = boxing.getRounds();
            sparringPartner = boxing.getSparringPartner();
            techniqueType = boxing.getTechniqueType();
        }

        return new WorkoutSummaryResponse(
                workout.getId(),
                workout.getType(),
                workout.getDurationMinutes(),
                workout.getMemo(),
                workout.getWorkoutDateTime(),
                distanceKm,
                place,
                caloriesBurned,
                rounds,
                sparringPartner,
                techniqueType
        );
    }
}
