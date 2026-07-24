package com.kdongdexample.norunnolifeexample.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@DiscriminatorValue("RUNNING")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunningWorkout extends Workout {

    private Double distanceKm;
    private String place;
    private Integer caloriesBurned;

    private RunningWorkout(Integer durationMinutes, String memo, LocalDateTime workoutDateTime,
                           Double distanceKm, String place, Integer caloriesBurned) {
        super(WorkoutType.RUNNING, durationMinutes, memo, workoutDateTime);
        this.distanceKm = distanceKm;
        this.place = place;
        this.caloriesBurned = caloriesBurned;
    }

    public static RunningWorkout create(Integer durationMinutes, String memo, LocalDateTime workoutDateTime,
                                        Double distanceKm, String place, Integer caloriesBurned) {
        return new RunningWorkout(durationMinutes, memo, workoutDateTime, distanceKm, place, caloriesBurned);
    }
}
