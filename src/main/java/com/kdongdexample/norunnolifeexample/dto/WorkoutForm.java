package com.kdongdexample.norunnolifeexample.dto;

import com.kdongdexample.norunnolifeexample.domain.TechniqueType;
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
        List<WorkoutDetailForm> details,

        // 러닝 전용
        @Positive
        Double distanceKm,
        String place,
        @Min(1)
        Integer caloriesBurned,

        // 복싱 전용
        @Min(1)
        Integer rounds,
        String sparringPartner,
        TechniqueType techniqueType
) {
        @AssertTrue(message = "러닝 기록에는 distanceKm이 필수입니다")
        public boolean isDistanceKmValidForRunning() {
                return type != WorkoutType.RUNNING || distanceKm != null;
        }

        @AssertTrue(message = "복싱 기록에는 rounds가 필수입니다")
        public boolean isRoundsValidForBoxing() {
                return type != WorkoutType.BOXING || rounds != null;
        }

        @AssertTrue(message = "복싱 기록에는 distanceKm/place/caloriesBurned를 보낼 수 없습니다")
        public boolean isNoRunningFieldsForBoxing() {
                return type != WorkoutType.BOXING
                        || (distanceKm == null && place == null && caloriesBurned == null);
        }

        @AssertTrue(message = "러닝 기록에는 rounds/sparringPartner/techniqueType을 보낼 수 없습니다")
        public boolean isNoBoxingFieldsForRunning() {
                return type != WorkoutType.RUNNING
                        || (rounds == null && sparringPartner == null && techniqueType == null);
        }
}
