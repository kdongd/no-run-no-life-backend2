package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import com.kdongdexample.norunnolifeexample.dto.WorkoutStatByType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutQueryRepository {
    Page<Workout> search(Long ownerId, WorkoutType type, LocalDateTime from, LocalDateTime to, Pageable pageable);
    List<WorkoutStatByType> statsByType(Long ownerId, LocalDateTime from, LocalDateTime to);
    List<WorkoutMonthlyStat> statsByMonth(Long ownerId, LocalDateTime from, LocalDateTime to);
}
