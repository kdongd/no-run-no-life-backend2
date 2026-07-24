package com.kdongdexample.norunnolifeexample.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private WorkoutType type;

    private Integer durationMinutes;
    private String memo;
    private LocalDateTime workoutDateTime;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutDetail> details = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    protected Workout(WorkoutType type, Integer durationMinutes, String memo, LocalDateTime workoutDateTime) {
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.memo = memo;
        this.workoutDateTime = workoutDateTime;
    }

    public void addDetail(WorkoutDetail detail) {
        details.add(detail);
        detail.assignWorkout(this);
    }

    public void clearDetails() {
        details.clear();
    }

    protected void updateCommon(Integer durationMinutes, String memo, LocalDateTime workoutDateTime) {
        this.durationMinutes = durationMinutes;
        this.memo = memo;
        this.workoutDateTime = workoutDateTime;
    }

    void assignId(Long id) {
        this.id = id;
    }
}
