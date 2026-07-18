package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JpaWorkoutRepositoryMySQLTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:mysql://localhost:3306/norunnolife_test");
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "ehdelwkdtk12@");
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

    }

    @Autowired
    JpaWorkoutRepository repository;

    @Test
    void statsByMonth_MySQL에서_extract가_정상_동작한다() {
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
