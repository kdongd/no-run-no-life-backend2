package com.kdongdexample.norunnolifeexample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdongdexample.norunnolifeexample.domain.Workout;
import com.kdongdexample.norunnolifeexample.domain.WorkoutType;
import com.kdongdexample.norunnolifeexample.dto.WorkoutForm;
import com.kdongdexample.norunnolifeexample.exception.WorkoutNotFoundException;
import com.kdongdexample.norunnolifeexample.service.WorkoutService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutController.class)
class WorkoutControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    WorkoutService service;

    @Test
    @DisplayName("GET /workouts 전체 목록을 조회할 수 있다")
    void getWorkouts() throws Exception {
        Workout workout = Workout.create(WorkoutType.RUNNING, 30, "메모", LocalDateTime.now());
        given(service.findAll()).willReturn(List.of(workout));

        mockMvc.perform(get("/workouts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /workouts 운동 기록을 등록할 수 있다")
    void createWorkout() throws Exception {
        WorkoutForm form = new WorkoutForm(WorkoutType.RUNNING, 30, "메모", LocalDateTime.now(), null);
        Workout workout = Workout.create(form.type(), form.durationMinutes(), form.memo(), form.workoutDateTime());
        given(service.save(any())).willReturn(workout);

        mockMvc.perform(post("/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("검증 실패 시 400을 반환한다")
    void createWorkout_validationFail() throws Exception {
        WorkoutForm form = new WorkoutForm(null, null, null, null, null);

        mockMvc.perform(post("/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /workouts/{id} 단건 조회 성공")
    void getWorkout() throws Exception {
        Workout workout = Workout.create(WorkoutType.RUNNING, 30, "메모", LocalDateTime.now());
        given(service.findById(1L)).willReturn(workout);

        mockMvc.perform(get("/workouts/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /workouts/{id} 없는 id 조회 시 404 반환")
    void getWorkout_notFound() throws Exception {
        given(service.findById(999L)).willThrow(new WorkoutNotFoundException(999L));

        mockMvc.perform(get("/workouts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /workouts/{id} 삭제 성공")
    void deleteWorkout() throws Exception {
        mockMvc.perform(delete("/workouts/1"))
                .andExpect(status().isOk());
    }
}