package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import com.kdongdexample.norunnolifeexample.dto.WorkoutStatByType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryWorkoutRepository implements WorkoutRepository {
    private Map<Long, Workout> store = new ConcurrentHashMap<>();
    private AtomicLong sequence = new AtomicLong(0);

    @Override
    public Workout save(Workout workout) {
        Long id = sequence.incrementAndGet();
        Workout saved = Workout.withId(id, workout);
        store.put(id, saved);
        return saved;
    }

    @Override
    public List<Workout> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<Workout> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void delete(Workout workout) {
        store.remove(workout.getId());
    }

    @Override
    public Page<Workout> search(WorkoutType type, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        throw new UnsupportedOperationException("MemoryWorkoutRepository는 검색 기능을 지원하지 않습니다.");
    }

    @Override
    public List<WorkoutStatByType> statsByType() {
        throw new UnsupportedOperationException("MemoryWorkoutRepository는 통계 기능을 지원하지 않습니다.");
    }

    @Override
    public List<WorkoutMonthlyStat> statsByMonth() {
        throw new UnsupportedOperationException("MemoryWorkoutRepository는 통계 기능을 지원하지 않습니다.");
    }
}