package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutIdAssigner;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryWorkoutRepository implements WorkoutRepository {
    private Map<Long, Workout> store = new ConcurrentHashMap<>();
    private AtomicLong sequence = new AtomicLong(0);

    @Override
    public Workout save(Workout workout) {
        Long id = sequence.incrementAndGet();
        WorkoutIdAssigner.assign(workout, id);
        store.put(id, workout);
        return workout;
    }

    @Override
    public Optional<Workout> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void delete(Workout workout) {
        store.remove(workout.getId());
    }
}
