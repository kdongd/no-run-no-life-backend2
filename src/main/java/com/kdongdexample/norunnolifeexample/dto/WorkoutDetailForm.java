package com.kdongdexample.norunnolifeexample.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkoutDetailForm(
        @NotNull @Min(1)
        Integer sequence,

        @NotNull @Size(max = 100)
        String label,

        @NotNull @Min(1) @Max(36000)
        Integer durationSeconds,

        @Size(max = 255)
        String note
) {}
