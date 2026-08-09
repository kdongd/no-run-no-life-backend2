package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.BoxingWorkout;
import com.kdongdexample.norunnolifeexample.domain.RunningWorkout;
import com.kdongdexample.norunnolifeexample.domain.TechniqueType;
import com.kdongdexample.norunnolifeexample.domain.Workout;

public record WorkoutTypeFields(
        Double distanceKm,
        String place,
        Integer caloriesBurned,
        Integer rounds,
        String sparringPartner,
        TechniqueType techniqueType
) {
    public static WorkoutTypeFields from(Workout workout) {
        if (workout instanceof RunningWorkout running) {
            return new WorkoutTypeFields(
                    running.getDistanceKm(), running.getPlace(), running.getCaloriesBurned(),
                    null, null, null);
        } else if (workout instanceof BoxingWorkout boxing) {
            return new WorkoutTypeFields(
                    null, null, null,
                    boxing.getRounds(), boxing.getSparringPartner(), boxing.getTechniqueType());
        }
        return new WorkoutTypeFields(null, null, null, null, null, null);
    }
}
