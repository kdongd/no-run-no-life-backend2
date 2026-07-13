package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import com.kdongdexample.norunnolifeexample.dto.WorkoutStatByType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JpaWorkoutRepositoryAdapter implements WorkoutRepository, WorkoutQueryRepository {

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
        return jpaWorkoutRepository.findAll(buildSpecification(type, from, to), pageable);
    }

    private Specification<Workout> buildSpecification(WorkoutType type, LocalDateTime from, LocalDateTime to) {
        List<Specification<Workout>> specs = new ArrayList<>();
        if (type != null) specs.add(WorkoutSpecifications.hasType(type));
        if (from != null) specs.add(WorkoutSpecifications.fromDate(from));
        if (to != null) specs.add(WorkoutSpecifications.toDate(to));

        return specs.stream()
                .reduce(Specification::and)
                .orElse((root, query, cb) -> cb.conjunction());
    }

    @Override
    public List<WorkoutStatByType> statsByType() {
        return jpaWorkoutRepository.statsByType();
    }

    @Override
    public List<WorkoutMonthlyStat> statsByMonth() {
        return jpaWorkoutRepository.statsByMonth();
    }
}