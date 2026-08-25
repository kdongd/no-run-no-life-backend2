package com.kdongdexample.norunnolifeexample.service;

import com.kdongdexample.norunnolifeexample.domain.BoxingWorkout;
import com.kdongdexample.norunnolifeexample.domain.RunningWorkout;
import com.kdongdexample.norunnolifeexample.domain.User;
import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutDetail;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutDetailForm;
import com.kdongdexample.norunnolifeexample.dto.WorkoutForm;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import com.kdongdexample.norunnolifeexample.dto.WorkoutStatByType;
import com.kdongdexample.norunnolifeexample.exception.InvalidSortPropertyException;
import com.kdongdexample.norunnolifeexample.exception.WorkoutNotFoundException;
import com.kdongdexample.norunnolifeexample.exception.WorkoutTypeMismatchException;
import com.kdongdexample.norunnolifeexample.repository.UserRepository;
import com.kdongdexample.norunnolifeexample.repository.WorkoutQueryRepository;
import com.kdongdexample.norunnolifeexample.repository.WorkoutRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Service
@Transactional(readOnly = true)
public class WorkoutService {

    private static final Set<String> ALLOWED_SORT_PROPERTIES =
            Set.of("workoutDateTime", "durationMinutes", "type", "distanceKm");

    private final WorkoutRepository repository;
    private final WorkoutQueryRepository queryRepository;
    private final UserRepository userRepository;

    public WorkoutService(WorkoutRepository repository, WorkoutQueryRepository queryRepository, UserRepository userRepository) {
        this.repository = repository;
        this.queryRepository = queryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Workout save(WorkoutForm form, Long userId) {
        User owner = resolveOwner(userId);

        Workout workout = switch (form.type()) {
            case RUNNING -> RunningWorkout.create(
                    owner, form.durationMinutes(), form.memo(), form.workoutDateTime(),
                    form.distanceKm(), form.place(), form.caloriesBurned());
            case BOXING -> BoxingWorkout.create(
                    owner, form.durationMinutes(), form.memo(), form.workoutDateTime(),
                    form.rounds(), form.sparringPartner(), form.techniqueType());
        };

        if (form.details() != null) {
            for (WorkoutDetailForm detailForm : form.details()) {
                WorkoutDetail detail = WorkoutDetail.create(workout, detailForm.sequence(), detailForm.label(), detailForm.durationSeconds(), detailForm.note());
                workout.addDetail(detail);
            }
        }

        return repository.save(workout);
    }

    public Workout findById(Long id, Long userId) {
        Workout workout = repository.findById(id).orElseThrow(() -> new WorkoutNotFoundException(id));
        validateOwner(workout, userId);
        return workout;
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Workout workout = repository.findById(id).orElseThrow(() -> new WorkoutNotFoundException(id));
        validateOwner(workout, userId);
        repository.delete(workout);
    }

    @Transactional
    public Workout update(Long id, WorkoutForm form, Long userId) {
        Workout workout = repository.findById(id).orElseThrow(() -> new WorkoutNotFoundException(id));
        validateOwner(workout, userId);

        if (workout.getType() != form.type()) {
            throw new WorkoutTypeMismatchException(workout.getType(), form.type());
        }

        if (workout instanceof RunningWorkout running) {
            running.update(form.durationMinutes(), form.memo(), form.workoutDateTime(),
                    form.distanceKm(), form.place(), form.caloriesBurned());
        } else if (workout instanceof BoxingWorkout boxing) {
            boxing.update(form.durationMinutes(), form.memo(), form.workoutDateTime(),
                    form.rounds(), form.sparringPartner(), form.techniqueType());
        }

        workout.clearDetails();
        if (form.details() != null) {
            for (WorkoutDetailForm detailForm : form.details()) {
                WorkoutDetail detail = WorkoutDetail.create(workout, detailForm.sequence(), detailForm.label(), detailForm.durationSeconds(), detailForm.note());
                workout.addDetail(detail);
            }
        }

        return workout;
    }

    public Page<Workout> search(WorkoutType type, LocalDateTime from, LocalDateTime to, Pageable pageable, Long userId) {
        validateSort(pageable.getSort());
        User owner = resolveOwner(userId);
        return queryRepository.search(owner, type, from, to, pageable);
    }

    private void validateSort(Sort sort) {
        sort.forEach(order -> {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new InvalidSortPropertyException(order.getProperty(), ALLOWED_SORT_PROPERTIES);
            }
        });
    }

    public List<WorkoutStatByType> statsByType(LocalDateTime from, LocalDateTime to, Long userId) {
        User owner = resolveOwner(userId);
        return queryRepository.statsByType(owner, from, to);
    }

    public List<WorkoutMonthlyStat> statsByMonth(LocalDateTime from, LocalDateTime to, Long userId) {
        User owner = resolveOwner(userId);
        return queryRepository.statsByMonth(owner, from, to);
    }

    private User resolveOwner(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("인증된 사용자를 찾을 수 없습니다. id: " + userId));
    }

    private void validateOwner(Workout workout, Long userId) {
        if (!workout.getOwner().getId().equals(userId)) {
            throw new WorkoutNotFoundException(workout.getId());
        }
    }

}
