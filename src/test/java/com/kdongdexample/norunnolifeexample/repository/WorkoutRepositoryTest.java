package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutDetail;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Workout 삭제 시 연관된 WorkoutDetail도 함께 삭제된다")
    void deleteWorkout_cascadeDeletesDetails() {
        Workout workout = Workout.create(WorkoutType.BOXING, 60, "스파링", LocalDateTime.now());
        WorkoutDetail detail = WorkoutDetail.create(workout, 1, "1라운드", 180, "섀도우");
        workout.addDetail(detail);
        Workout saved = jpaWorkoutRepository.save(workout);
        Long savedId = saved.getId();

        jpaWorkoutRepository.delete(saved);
        jpaWorkoutRepository.flush();

        Optional<Workout> found = jpaWorkoutRepository.findById(savedId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("memo가 255자를 초과하면 DB 저장 시 예외가 발생한다")
    void memo가_255자_초과하면_예외발생() {
        String longMemo = "가".repeat(300);
        Workout workout = Workout.create(WorkoutType.RUNNING, 30, longMemo, LocalDateTime.now());

        assertThatThrownBy(() -> {
            jpaWorkoutRepository.save(workout);
            jpaWorkoutRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}