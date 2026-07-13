package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class WorkoutSpecifications {

    private WorkoutSpecifications() {}

    public static Specification<Workout> hasType(WorkoutType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Workout> fromDate(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("workoutDateTime"), from);
    }

    public static Specification<Workout> toDate(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("workoutDateTime"), to);
    }
}