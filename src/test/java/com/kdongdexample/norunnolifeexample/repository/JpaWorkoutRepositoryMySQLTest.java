package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.RunningWorkout;
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
        RunningWorkout workout = RunningWorkout.create(
                30, "MySQL 테스트", LocalDateTime.of(2026, 7, 16, 10, 0),
                5.0, "한강", 300
        );
        repository.save(workout);

        List<WorkoutMonthlyStat> result = repository.statsByMonth();

        assertThat(result).isNotEmpty();
        assertThat(result)
                .anySatisfy(stat -> {
                    assertThat(stat.year()).isEqualTo(2026);
                    assertThat(stat.month()).isEqualTo(7);
                });
    }
}
