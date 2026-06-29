package com.kdongdexample.norunnolifeexample.dto;

public record WorkoutDetailForm(
        Integer sequence,
        String label,
        Integer durationSeconds,
        String note
) {}
