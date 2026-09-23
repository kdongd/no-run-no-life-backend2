package com.kdongdexample.norunnolifeexample.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "user_profiles")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 인증 도메인(User)과 프로필 데이터를 분리 - User는 로그인/토큰에만 관여하고
    // 이 테이블은 AI 코칭에 쓰이는 신체/목표 정보만 담당합니다. 가입 직후엔 이 row가 없는 상태이고
    // 프로필 작성 API를 호출해야 생성됩니다.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private Integer age;
    private Double heightCm;
    private Double weightKg;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experienceLevel;

    // 러닝/복싱 둘 다 할 수 있어서 다중 선택으로 설계 했습니다. 실제 운동 기록(Workout)의 종목과는 별개로
    // 코칭을 어떤 종목을 중심으로 받고 싶은지를 나타내는 프로필 값입니다.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_profile_primary_exercises", joinColumns = @JoinColumn(name = "user_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type")
    private Set<WorkoutType> primaryExercises = EnumSet.noneOf(WorkoutType.class);

    private String goal;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private UserProfile(User user, Gender gender, Integer age, Double heightCm, Double weightKg,
                        ExperienceLevel experienceLevel, Set<WorkoutType> primaryExercises, String goal) {
        this.user = user;
        this.gender = gender;
        this.age = age;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.experienceLevel = experienceLevel;
        this.primaryExercises = (primaryExercises == null || primaryExercises.isEmpty())
                ? EnumSet.noneOf(WorkoutType.class)
                : EnumSet.copyOf(primaryExercises);
        this.goal = goal;
    }

    public static UserProfile create(User user, Gender gender, Integer age, Double heightCm, Double weightKg,
                                     ExperienceLevel experienceLevel, Set<WorkoutType> primaryExercises, String goal) {
        return new UserProfile(user, gender, age, heightCm, weightKg, experienceLevel, primaryExercises, goal);
    }
}
