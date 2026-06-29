package com.kdongdexample.norunnolifeexample.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutTest {

    @Test
    @DisplayName("Workout.create()로 운동 기록을 생성할 수 있다")
    void create_운동_생성() {
        LocalDateTime now = LocalDateTime.now();
        Workout workout = Workout.create(WorkoutType.RUNNING, 30, "테스트 메모", now);

        assertThat(workout.getType()).isEqualTo(WorkoutType.RUNNING);
        assertThat(workout.getDurationMinutes()).isEqualTo(30);
        assertThat(workout.getMemo()).isEqualTo("테스트 메모");
        assertThat(workout.getWorkoutDateTime()).isEqualTo(now);
    }

    @Test
    @DisplayName("addDetail()로 WorkoutDetail을 추가할 수 있다")
    void addDetail_디테일_추가() {
        Workout workout = Workout.create(WorkoutType.BOXING, 60, null, LocalDateTime.now());
        WorkoutDetail detail = WorkoutDetail.create(workout, 1, "1라운드", 180, "섀도우");

        workout.addDetail(detail);

        assertThat(workout.getDetails()).hasSize(1);
        assertThat(workout.getDetails().get(0).getLabel()).isEqualTo("1라운드");
    }
}