package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.WorkoutType;

public record WorkoutStatByType(
        WorkoutType type,
        Long count,
        Long totalDurationMinutes
) {}