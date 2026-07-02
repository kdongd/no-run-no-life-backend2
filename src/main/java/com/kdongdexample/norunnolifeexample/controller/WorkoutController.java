package com.kdongdexample.norunnolifeexample.controller;

import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.dto.WorkoutForm;
import com.kdongdexample.norunnolifeexample.dto.WorkoutResponse;
import com.kdongdexample.norunnolifeexample.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
public class WorkoutController {
    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    @GetMapping("/workouts")
    public ResponseEntity<List<WorkoutResponse>> getWorkouts() {
        List<WorkoutResponse> responses = service.findAll().stream()
                .map(WorkoutResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
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

    @DeleteMapping("/workouts/{id}")
    public void deleteWorkout(@PathVariable Long id) {
        service.delete(id);
    }
}