package com.kdongdexample.norunnolifeexample.controller;

import com.kdongdexample.norunnolifeexample.config.SecurityConfig;
import com.kdongdexample.norunnolifeexample.security.JwtTokenProvider;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdongdexample.norunnolifeexample.domain.BoxingWorkout;
import com.kdongdexample.norunnolifeexample.domain.RunningWorkout;
import com.kdongdexample.norunnolifeexample.domain.TechniqueType;
import com.kdongdexample.norunnolifeexample.domain.User;
import com.kdongdexample.norunnolifeexample.domain.WorkoutDetail;
import com.kdongdexample.norunnolifeexample.dto.WorkoutDetailForm;
import com.kdongdexample.norunnolifeexample.dto.WorkoutForm;
import com.kdongdexample.norunnolifeexample.exception.WorkoutNotFoundException;
import com.kdongdexample.norunnolifeexample.service.WorkoutService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(WorkoutController.class)
@Import({SecurityConfig.class, JwtTokenProvider.class})
class WorkoutControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    WorkoutService service;

    private final User owner = User.create("test@test.com", "encoded-password");

    private static UsernamePasswordAuthenticationToken authAs(Long userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, List.of());
    }

    @Test
    @DisplayName("GET /workouts 전체 목록을 조회할 수 있다")
    void getWorkouts() throws Exception {
        RunningWorkout workout = RunningWorkout.create(owner, 30, "메모", LocalDateTime.now(), 5.0, "한강", 300);
        given(service.search(any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(workout)));

        mockMvc.perform(get("/workouts").with(authentication(authAs(1L))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /workouts 운동 기록을 등록할 수 있다")
    void createWorkout() throws Exception {
        WorkoutForm form = new WorkoutForm(
                com.kdongdexample.norunnolifeexample.domain.WorkoutType.RUNNING,
                30, "메모", LocalDateTime.now(), null,
                5.0, "한강", 300,
                null, null, null);
        RunningWorkout workout = RunningWorkout.create(
                owner, form.durationMinutes(), form.memo(), form.workoutDateTime(),
                form.distanceKm(), form.place(), form.caloriesBurned());
        ReflectionTestUtils.setField(workout, "id", 1L);
        given(service.save(any(), any())).willReturn(workout);

        mockMvc.perform(post("/workouts")
                        .with(authentication(authAs(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/workouts/1")));
    }

    @Test
    @DisplayName("검증 실패 시 400을 반환한다")
    void createWorkout_validationFail() throws Exception {
        WorkoutForm form = new WorkoutForm(
                null, null, null, null, null,
                null, null, null,
                null, null, null);

        mockMvc.perform(post("/workouts")
                        .with(authentication(authAs(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("복싱 타입인데 러닝 전용 필드(distanceKm)를 같이 보내면 400을 반환한다")
    void createWorkout_boxingWithRunningFields_returns400() throws Exception {
        WorkoutForm form = new WorkoutForm(
                com.kdongdexample.norunnolifeexample.domain.WorkoutType.BOXING,
                60, "메모", LocalDateTime.now(), null,
                5.0, null, null,   // distanceKm이 섞여 들어옴
                3, "파트너", TechniqueType.SPARRING);

        mockMvc.perform(post("/workouts")
                        .with(authentication(authAs(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("러닝 타입인데 복싱 전용 필드(rounds)를 같이 보내면 400을 반환한다")
    void createWorkout_runningWithBoxingFields_returns400() throws Exception {
        WorkoutForm form = new WorkoutForm(
                com.kdongdexample.norunnolifeexample.domain.WorkoutType.RUNNING,
                30, "메모", LocalDateTime.now(), null,
                5.0, "한강", 300,
                3, null, null);   // rounds가 섞여 들어옴

        mockMvc.perform(post("/workouts")
                        .with(authentication(authAs(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /workouts/{id} 단건 조회 성공")
    void getWorkout() throws Exception {
        RunningWorkout workout = RunningWorkout.create(owner, 30, "메모", LocalDateTime.now(), 5.0, "한강", 300);
        given(service.findById(1L, 1L)).willReturn(workout);

        mockMvc.perform(get("/workouts/1").with(authentication(authAs(1L))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /workouts/{id} 없는 id 조회 시 404 반환")
    void getWorkout_notFound() throws Exception {
        given(service.findById(999L, 1L)).willThrow(new WorkoutNotFoundException(999L));

        mockMvc.perform(get("/workouts/999").with(authentication(authAs(1L))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /workouts/{id} 삭제 성공")
    void deleteWorkout() throws Exception {
        mockMvc.perform(delete("/workouts/1").with(authentication(authAs(1L))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /workouts/{id} details가 있어도 순환참조 없이 정상 직렬화된다")
    void getWorkout_withDetails_noCircularReference() throws Exception {
        BoxingWorkout workout = BoxingWorkout.create(owner, 60, "스파링", LocalDateTime.now(), 3, "파트너", TechniqueType.SPARRING);
        WorkoutDetail detail = WorkoutDetail.create(workout, 1, "1라운드", 180, "섀도우");
        workout.addDetail(detail);
        given(service.findById(1L, 1L)).willReturn(workout);

        mockMvc.perform(get("/workouts/1").with(authentication(authAs(1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[0].label").value("1라운드"))
                .andExpect(jsonPath("$.details[0].workout").doesNotExist());
    }

    @Test
    @DisplayName("details 내부 값이 검증 실패하면 400을 반환한다")
    void createWorkout_detailsValidationFail() throws Exception {
        List<WorkoutDetailForm> invalidDetails = List.of(
                new WorkoutDetailForm(null, null, null, null)
        );
        WorkoutForm form = new WorkoutForm(
                com.kdongdexample.norunnolifeexample.domain.WorkoutType.BOXING,
                60, "메모", LocalDateTime.now(), invalidDetails,
                null, null, null,
                3, "파트너", TechniqueType.SPARRING);

        mockMvc.perform(post("/workouts")
                        .with(authentication(authAs(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest());
    }
}
