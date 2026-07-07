package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaWorkoutRepository extends JpaRepository<Workout, Long> {

    @Query("select distinct w from Workout w left join fetch w.details")
    List<Workout> findAllWithDetails();

    @Query("select w from Workout w left join fetch w.details where w.id = :id")
    Optional<Workout> findByIdWithDetails(@Param("id") Long id);

    Page<Workout> findByType(WorkoutType type, Pageable pageable);

    Page<Workout> findByWorkoutDateTimeBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Workout> findByWorkoutDateTimeAfter(LocalDateTime from, Pageable pageable);

    Page<Workout> findByWorkoutDateTimeBefore(LocalDateTime to, Pageable pageable);

    Page<Workout> findByTypeAndWorkoutDateTimeBetween(WorkoutType type, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Workout> findByTypeAndWorkoutDateTimeAfter(WorkoutType type, LocalDateTime from, Pageable pageable);

    Page<Workout> findByTypeAndWorkoutDateTimeBefore(WorkoutType type, LocalDateTime to, Pageable pageable);
}