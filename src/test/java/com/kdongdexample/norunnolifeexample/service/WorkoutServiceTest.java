package com.kdongdexample.norunnolifeexample.service;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutDetailForm;
import com.kdongdexample.norunnolifeexample.dto.WorkoutForm;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import com.kdongdexample.norunnolifeexample.dto.WorkoutStatByType;
import com.kdongdexample.norunnolifeexample.exception.InvalidSortPropertyException;
import com.kdongdexample.norunnolifeexample.exception.WorkoutNotFoundException;
import com.kdongdexample.norunnolifeexample.repository.WorkoutQueryRepository;
import com.kdongdexample.norunnolifeexample.repository.WorkoutRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository repository;

    @Mock
    private WorkoutQueryRepository queryRepository;

    private final LocalDateTime now = LocalDateTime.now();

    private WorkoutService service() {
        return new WorkoutService(repository, queryRepository);
    }

    @Test
    @DisplayName("운동 기록 저장 후 반환")
    void save() {
        WorkoutForm form = new WorkoutForm(WorkoutType.RUNNING, 30, "테스트 메모", now, null);
        Workout saved = Workout.create(WorkoutType.RUNNING, 30, "테스트 메모", now);
        given(repository.save(any())).willReturn(saved);

        Workout result = service().save(form);

        assertThat(result.getType()).isEqualTo(WorkoutType.RUNNING);
        assertThat(result.getDurationMinutes()).isEqualTo(30);
        assertThat(result.getMemo()).isEqualTo("테스트 메모");
    }

    @Test
    @DisplayName("details 포함 운동 기록 저장")
    void save_withDetails() {
        List<WorkoutDetailForm> details = List.of(
                new WorkoutDetailForm(1, "1라운드", 180, "섀도우")
        );
        WorkoutForm form = new WorkoutForm(WorkoutType.BOXING, 60, "메모", now, details);
        Workout saved = Workout.create(WorkoutType.BOXING, 60, "메모", now);
        given(repository.save(any())).willReturn(saved);

        Workout result = service().save(form);

        assertThat(result.getType()).isEqualTo(WorkoutType.BOXING);
    }

    @Test
    @DisplayName("존재하는 id 조회 성공")
    void findById_success() {
        Workout workout = Workout.create(WorkoutType.RUNNING, 30, "메모", now);
        given(repository.findById(1L)).willReturn(Optional.of(workout));

        Workout result = service().findById(1L);

        assertThat(result.getType()).isEqualTo(WorkoutType.RUNNING);
    }

    @Test
    @DisplayName("존재하지 않는 id 조회 시 WorkoutNotFoundException 발생")
    void findById_notFound() {
        given(repository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().findById(999L))
                .isInstanceOf(WorkoutNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 id 삭제 시 WorkoutNotFoundException 발생")
    void delete_notFound() {
        given(repository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service().delete(999L))
                .isInstanceOf(WorkoutNotFoundException.class);
    }

    @Test
    @DisplayName("존재하는 id 삭제 성공")
    void delete_success() {
        Workout workout = Workout.create(WorkoutType.RUNNING, 30, "메모", now);
        given(repository.findById(1L)).willReturn(Optional.of(workout));

        assertThatCode(() -> service().delete(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("허용된 정렬 필드로 검색하면 queryRepository.search()에 위임한다")
    void search_validSort_delegatesToQueryRepository() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("workoutDateTime"));
        Workout workout = Workout.create(WorkoutType.RUNNING, 30, "메모", now);
        Page<Workout> page = new PageImpl<>(List.of(workout));
        given(queryRepository.search(WorkoutType.RUNNING, null, null, pageable)).willReturn(page);

        Page<Workout> result = service().search(WorkoutType.RUNNING, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(WorkoutType.RUNNING);
    }

    @Test
    @DisplayName("허용되지 않은 정렬 필드로 검색하면 InvalidSortPropertyException이 발생한다")
    void search_invalidSort_throwsException() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("memo"));

        assertThatThrownBy(() -> service().search(null, null, null, pageable))
                .isInstanceOf(InvalidSortPropertyException.class);
    }

    @Test
    @DisplayName("정렬 필드가 화이트리스트에 없으면 queryRepository.search()가 호출되지 않는다")
    void search_invalidSort_doesNotCallRepository() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));

        assertThatThrownBy(() -> service().search(null, null, null, pageable))
                .isInstanceOf(InvalidSortPropertyException.class);

        verify(queryRepository, never()).search(any(), any(), any(), any());
    }

    @Test
    @DisplayName("타입별 통계는 queryRepository에 위임한다")
    void statsByType_delegatesToQueryRepository() {
        List<WorkoutStatByType> stats = List.of(new WorkoutStatByType(WorkoutType.RUNNING, 3L, 90L));
        given(queryRepository.statsByType()).willReturn(stats);

        List<WorkoutStatByType> result = service().statsByType();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(WorkoutType.RUNNING);
    }

    @Test
    @DisplayName("월별 통계는 queryRepository에 위임한다")
    void statsByMonth_delegatesToQueryRepository() {
        List<WorkoutMonthlyStat> stats = List.of(new WorkoutMonthlyStat(2026, 5, 4L));
        given(queryRepository.statsByMonth()).willReturn(stats);

        List<WorkoutMonthlyStat> result = service().statsByMonth();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).year()).isEqualTo(2026);
    }
}
