package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkoutRepository {
    Workout save(Workout workout);
    Optional<Workout> findById(Long id);
    List<Workout> findAll();
    void delete(Workout workout);

    List<Workout> search(WorkoutType type, LocalDateTime from, LocalDateTime to, Sort sort);
}