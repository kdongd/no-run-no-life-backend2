package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

public record WorkoutForm(
        @NotNull
        WorkoutType type,

        @NotNull @Min(1) @Max(600)
        Integer durationMinutes,

        @Size(max = 255)
        String memo,

        @NotNull @PastOrPresent
        LocalDateTime workoutDateTime,

        @Valid
        List<WorkoutDetailForm> details
) {}
