package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutDetail;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WorkoutRepositoryTest {

    @Autowired
    JpaWorkoutRepository jpaWorkoutRepository;

    @Test
    @DisplayName("운동 기록을 저장하고 조회할 수 있다")
    void saveAndFind() {
        Workout workout = Workout.create(WorkoutType.RUNNING, 30, "테스트", LocalDateTime.now());
        jpaWorkoutRepository.save(workout);

        List<Workout> result = jpaWorkoutRepository.findAllWithDetails();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(WorkoutType.RUNNING);
    }

    @Test
    @DisplayName("detail 포함해서 저장하고 조회할 수 있다")
    void saveWithDetails() {
        Workout workout = Workout.create(WorkoutType.BOXING, 60, "스파링", LocalDateTime.now());
        WorkoutDetail detail = WorkoutDetail.create(workout, 1, "1라운드", 180, "섀도우");
        workout.addDetail(detail);
        jpaWorkoutRepository.save(workout);

        Optional<Workout> result = jpaWorkoutRepository.findByIdWithDetails(workout.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDetails()).hasSize(1);
    }
}