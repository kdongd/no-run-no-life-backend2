package com.kdongdexample.norunnolifeexample.repository;

import com.kdongdexample.norunnolifeexample.domain.BoxingWorkout;
import com.kdongdexample.norunnolifeexample.domain.RunningWorkout;
import com.kdongdexample.norunnolifeexample.domain.TechniqueType;
import com.kdongdexample.norunnolifeexample.domain.Workout;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@Import(JpaWorkoutRepositoryAdapter.class)
class JpaWorkoutRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private JpaWorkoutRepositoryAdapter adapter;

    @Test
    @DisplayName("distanceKm으로 정렬하면 복싱 기록도 포함되고 null은 마지막으로 온다")
    void sortByDistanceKmIncludesBoxingWorkoutsWithNullsLast() {
        // given
        RunningWorkout running10km = RunningWorkout.create(
                60, "메모", LocalDateTime.now().minusDays(1), 10.0, "한강", 500);
        RunningWorkout running5km = RunningWorkout.create(
                30, "메모", LocalDateTime.now().minusDays(2), 5.0, "한강", 300);
        BoxingWorkout boxing = BoxingWorkout.create(
                45, "메모", LocalDateTime.now().minusDays(3), 3, "파트너", TechniqueType.SPARRING);

        adapter.save(running10km);
        adapter.save(running5km);
        adapter.save(boxing);

        // when
        Page<Workout> result = adapter.search(
                null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "distanceKm")));

        // then
        List<Workout> content = result.getContent();
        assertThat(content).hasSize(3);
        assertThat(content.get(0)).isEqualTo(running5km);
        assertThat(content.get(1)).isEqualTo(running10km);
        assertThat(content.get(2)).isEqualTo(boxing); // distanceKm null -> 마지막
    }

    @Test
    @DisplayName("distanceKm 정렬 시 count 쿼리도 정상적으로 전체 개수를 반환한다")
    void countQueryReturnsCorrectTotalWhenSortingByDistanceKm() {
        // given
        adapter.save(RunningWorkout.create(
                60, "메모", LocalDateTime.now(), 10.0, "한강", 500));
        adapter.save(RunningWorkout.create(
                30, "메모", LocalDateTime.now(), 5.0, "한강", 300));
        adapter.save(BoxingWorkout.create(
                45, "메모", LocalDateTime.now(), 3, "파트너", TechniqueType.SPARRING));

        // when
        Page<Workout> result = adapter.search(
                null, null, null,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "distanceKm")));

        // then
        // order by가 count 쿼리에 새었다면 totalElements가 틀어지거나 예외 발생
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
    }
}
