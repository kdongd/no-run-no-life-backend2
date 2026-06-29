package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
@Primary
public class JpaWorkoutRepositoryAdapter implements WorkoutRepository{

    JpaWorkoutRepository jpaWorkoutRepository;

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
}
