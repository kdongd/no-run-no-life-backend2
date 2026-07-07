package com.kdongdexample.norunnolifeexample.exception;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse response = new ErrorResponse(400, "입력값이 올바르지 않습니다", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(WorkoutNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWorkoutNotFoundException(WorkoutNotFoundException e) {
        ErrorResponse response = new ErrorResponse(404, e.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReference(PropertyReferenceException e) {
        ErrorResponse response = new ErrorResponse(400, "정렬 필드가 올바르지 않습니다: " + e.getPropertyName(), List.of());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        ErrorResponse response = new ErrorResponse(400, "요청 파라미터 형식이 올바르지 않습니다: " + e.getName(), List.of());
        return ResponseEntity.badRequest().body(response);
    }
}