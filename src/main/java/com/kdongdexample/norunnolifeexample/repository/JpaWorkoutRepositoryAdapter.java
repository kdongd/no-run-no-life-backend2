package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JpaWorkoutRepositoryAdapter implements WorkoutRepository {

    private final JpaWorkoutRepository jpaWorkoutRepository;

    public JpaWorkoutRepositoryAdapter(JpaWorkoutRepository jpaWorkoutRepository) {
        this.jpaWorkoutRepository = jpaWorkoutRepository;
    }

    @Override
    public Workout save(Workout workout) {
        return jpaWorkoutRepository.save(workout);
    }

    @Override
    public List<Workout> findAll() {
        return jpaWorkoutRepository.findAllWithDetails();
    }

    @Override
    public Optional<Workout> findById(Long id) {
        return jpaWorkoutRepository.findByIdWithDetails(id);
    }

    @Override
    public void delete(Workout workout) {
        jpaWorkoutRepository.delete(workout);
    }

    @Override
    public Page<Workout> search(WorkoutType type, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        boolean hasType = type != null;
        boolean hasFrom = from != null;
        boolean hasTo = to != null;

        if (hasType && hasFrom && hasTo) {
            return jpaWorkoutRepository.findByTypeAndWorkoutDateTimeBetween(type, from, to, pageable);
        }
        if (hasType && hasFrom) {
            return jpaWorkoutRepository.findByTypeAndWorkoutDateTimeAfter(type, from, pageable);
        }
        if (hasType && hasTo) {
            return jpaWorkoutRepository.findByTypeAndWorkoutDateTimeBefore(type, to, pageable);
        }
        if (hasType) {
            return jpaWorkoutRepository.findByType(type, pageable);
        }
        if (hasFrom && hasTo) {
            return jpaWorkoutRepository.findByWorkoutDateTimeBetween(from, to, pageable);
        }
        if (hasFrom) {
            return jpaWorkoutRepository.findByWorkoutDateTimeAfter(from, pageable);
        }
        if (hasTo) {
            return jpaWorkoutRepository.findByWorkoutDateTimeBefore(to, pageable);
        }
        return jpaWorkoutRepository.findAll(pageable);
    }
}