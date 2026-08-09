package com.kdongdexample.norunnolifeexample.controller;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutForm;
import com.kdongdexample.norunnolifeexample.dto.WorkoutMonthlyStat;
import com.kdongdexample.norunnolifeexample.dto.WorkoutResponse;
import com.kdongdexample.norunnolifeexample.dto.WorkoutStatByType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutSummaryResponse;
import com.kdongdexample.norunnolifeexample.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class WorkoutController {
    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    @GetMapping("/workouts")
    public ResponseEntity<Page<WorkoutSummaryResponse>> searchWorkouts(
            @RequestParam(required = false) WorkoutType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "workoutDateTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<WorkoutSummaryResponse> page = service.search(type, from, to, pageable)
                .map(WorkoutSummaryResponse::from);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/workouts")
    public ResponseEntity<WorkoutResponse> createWorkout(@Valid @RequestBody WorkoutForm form) {
        Workout saved = service.save(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(WorkoutResponse.from(saved));
    }

    @GetMapping("/workouts/{id}")
    public ResponseEntity<WorkoutResponse> getWorkout(@PathVariable Long id) {
        return ResponseEntity.ok(WorkoutResponse.from(service.findById(id)));
    }

    @PutMapping("/workouts/{id}")
    public ResponseEntity<WorkoutResponse> updateWorkout(@PathVariable Long id, @Valid @RequestBody WorkoutForm form) {
        Workout updated = service.update(id, form);
        return ResponseEntity.ok(WorkoutResponse.from(updated));
    }

    @DeleteMapping("/workouts/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/workouts/stats/by-type")
    public ResponseEntity<List<WorkoutStatByType>> getStatsByType() {
        return ResponseEntity.ok(service.statsByType());
    }

    @GetMapping("/workouts/stats/monthly")
    public ResponseEntity<List<WorkoutMonthlyStat>> getStatsByMonth() {
        return ResponseEntity.ok(service.statsByMonth());
    }
}
