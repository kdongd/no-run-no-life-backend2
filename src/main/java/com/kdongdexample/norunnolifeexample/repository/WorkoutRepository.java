package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;

import java.util.Optional;

public interface WorkoutRepository {
    Workout save(Workout workout);
    Optional<Workout> findById(Long id);
    void delete(Workout workout);
}