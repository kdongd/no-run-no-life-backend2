package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import org.springframework.data.domain.Sort;
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

    // type만
    List<Workout> findByType(WorkoutType type, Sort sort);

    // 기간만 — 둘 다 있음
    List<Workout> findByWorkoutDateTimeBetween(LocalDateTime from, LocalDateTime to, Sort sort);

    // 기간만 — from만 (그 이후 전부)
    List<Workout> findByWorkoutDateTimeAfter(LocalDateTime from, Sort sort);

    // 기간만 — to만 (그 이전 전부)
    List<Workout> findByWorkoutDateTimeBefore(LocalDateTime to, Sort sort);

    // type + 기간 둘 다
    List<Workout> findByTypeAndWorkoutDateTimeBetween(WorkoutType type, LocalDateTime from, LocalDateTime to, Sort sort);

    // type + from만
    List<Workout> findByTypeAndWorkoutDateTimeAfter(WorkoutType type, LocalDateTime from, Sort sort);

    // type + to만
    List<Workout> findByTypeAndWorkoutDateTimeBefore(WorkoutType type, LocalDateTime to, Sort sort);

    @Query("select w from Workout w where w.type = :type and w.workoutDateTime between :from and :to")
    List<Workout> findByTypeAndDateRangeJpql(@Param("type") WorkoutType type,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             Sort sort);
}