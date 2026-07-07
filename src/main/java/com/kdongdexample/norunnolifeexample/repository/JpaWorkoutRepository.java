package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import com.kdongdexample.norunnolifeexample.dto.WorkoutStatByType;
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

    @Query("select new com.kdongdexample.norunnolifeexample.dto.WorkoutStatByType(" +
            "w.type, count(w), sum(w.durationMinutes)) " +
            "from Workout w group by w.type")
    List<WorkoutStatByType> statsByType();

    @Query("select new com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat(" +
            "cast(function('YEAR', w.workoutDateTime) as integer), " +
            "cast(function('MONTH', w.workoutDateTime) as integer), " +
            "count(w)) " +
            "from Workout w group by function('YEAR', w.workoutDateTime), function('MONTH', w.workoutDateTime) " +
            "order by function('YEAR', w.workoutDateTime), function('MONTH', w.workoutDateTime)")
    List<WorkoutMonthlyStat> statsByMonth();
}