package com.kdongdexample.norunnolifeexample.service;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutDetail;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutDetailForm;
import com.kdongdexample.norunnolifeexample.dto.WorkoutForm;
import com.kdongdexample.norunnolifeexample.exception.WorkoutNotFoundException;
import com.kdongdexample.norunnolifeexample.repository.WorkoutRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class WorkoutService {
    private final WorkoutRepository repository;

    public WorkoutService(WorkoutRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Workout save(WorkoutForm form) {
        Workout workout = Workout.create(form.type(), form.durationMinutes(), form.memo(), form.workoutDateTime());

        if (form.details() != null) {
            for (WorkoutDetailForm detailForm : form.details()) {
                WorkoutDetail detail = WorkoutDetail.create(workout, detailForm.sequence(), detailForm.label(), detailForm.durationSeconds(), detailForm.note());
                workout.addDetail(detail);
            }
        }

        return repository.save(workout);
    }

    public List<Workout> findAll() {
        return repository.findAll();
    }

    public Workout findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new WorkoutNotFoundException(id));
    }

    @Transactional
    public void delete(Long id) {
        Workout workout = repository.findById(id).orElseThrow(() -> new WorkoutNotFoundException(id));
        repository.delete(workout);
    }

    public List<Workout> search(WorkoutType type, LocalDateTime from, LocalDateTime to, Sort sort) {
        return repository.search(type, from, to, sort);
    }
}