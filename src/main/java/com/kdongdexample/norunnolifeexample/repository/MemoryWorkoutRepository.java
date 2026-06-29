package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryWorkoutRepository implements WorkoutRepository {
    private Map<Long, Workout> store = new ConcurrentHashMap<>();
    private AtomicLong sequence = new AtomicLong(0);

    @Override
    public Workout save(Workout workout) {
        long id = sequence.incrementAndGet();
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
}