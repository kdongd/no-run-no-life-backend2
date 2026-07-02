package com.kdongdexample.norunnolifeexample.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private WorkoutType type;

    private Integer durationMinutes;
    private String memo;
    private LocalDateTime workoutDateTime;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutDetail> details = new ArrayList<>();

    public static Workout create(WorkoutType type, Integer durationMinutes, String memo, LocalDateTime workoutDateTime) {
        Workout workout = new Workout();
        workout.type = type;
        workout.durationMinutes = durationMinutes;
        workout.memo = memo;
        workout.workoutDateTime = workoutDateTime;
        return workout;
    }

    public static Workout withId(long id, Workout workout) {
        Workout saved = new Workout();
        saved.id = id;
        saved.type = workout.type;
        saved.durationMinutes = workout.durationMinutes;
        saved.memo = workout.memo;
        saved.workoutDateTime = workout.workoutDateTime;

        for (WorkoutDetail detail : workout.details) {
            saved.addDetail(detail);
        }

        return saved;
    }

    public void addDetail(WorkoutDetail detail) {
        details.add(detail);
        detail.assignWorkout(this);
    }

}
