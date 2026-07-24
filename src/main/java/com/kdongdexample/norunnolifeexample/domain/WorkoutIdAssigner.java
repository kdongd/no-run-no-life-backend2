package com.kdongdexample.norunnolifeexample.domain;

public class WorkoutIdAssigner {

    private WorkoutIdAssigner() {}

    public static void assign(Workout workout, Long id) {
        workout.assignId(id);
    }
}
