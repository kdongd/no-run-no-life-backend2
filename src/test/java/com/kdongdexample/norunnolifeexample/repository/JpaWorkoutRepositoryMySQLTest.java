package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class JpaWorkoutRepositoryMySQLTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    JpaWorkoutRepository repository;

    @Test
    void statsByMonth_shouldExtractYearAndMonth_onMySQL() {
        Workout workout = Workout.create(
                WorkoutType.RUNNING, 30, "MySQL 테스트",
                LocalDateTime.of(2026, 7, 16, 10, 0)
        );
        repository.save(workout);

        List<WorkoutMonthlyStat> result = repository.statsByMonth();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).year()).isEqualTo(2026);
        assertThat(result.get(0).month()).isEqualTo(7);
    }
}
