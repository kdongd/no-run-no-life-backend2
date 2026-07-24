package com.kdongdexample.norunnolifeexample.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@DiscriminatorValue("BOXING")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoxingWorkout extends Workout {

    private Integer rounds;
    private String sparringPartner;

    @Enumerated(EnumType.STRING)
    private TechniqueType techniqueType;

    private BoxingWorkout(Integer durationMinutes, String memo, LocalDateTime workoutDateTime,
                          Integer rounds, String sparringPartner, TechniqueType techniqueType) {
        super(WorkoutType.BOXING, durationMinutes, memo, workoutDateTime);
        this.rounds = rounds;
        this.sparringPartner = sparringPartner;
        this.techniqueType = techniqueType;
    }

    public static BoxingWorkout create(Integer durationMinutes, String memo, LocalDateTime workoutDateTime,
                                       Integer rounds, String sparringPartner, TechniqueType techniqueType) {
        return new BoxingWorkout(durationMinutes, memo, workoutDateTime, rounds, sparringPartner, techniqueType);
    }
}
