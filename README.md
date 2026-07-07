# 🥊 NO RUN NO LIFE
> 기록이 실력을 만든다. 러닝과 복싱 기록을 남기는 웹 서비스.
---
## 📌 프로젝트 소개
- 러닝과 복싱, 두 종목의 기록을 한 곳에서 관리하는 Spring Boot REST API 서버
- Spring MVC, JPA를 단계별로 적용하며 처음부터 직접 만들어가는 프로젝트
---
## 🛠 기술 스택
| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5 |
| API | REST API |
| Build | Gradle |
| DB | H2 (In-Memory) |
| ORM | Spring Data JPA / Hibernate |
---
## ⚙️ 실행 방법
```bash
./gradlew bootRun
```
서버 실행 후 `http://localhost:8080` 에서 API 사용 가능  
H2 콘솔: `http://localhost:8080/h2-console`
---
## 📁 프로젝트 구조

src/main/java/com/kdongdexample/norunnolifeexample
├── controller
│   └── WorkoutController.java
├── domain
│   ├── Workout.java
│   ├── WorkoutDetail.java
│   └── WorkoutType.java
├── dto
│   ├── WorkoutForm.java
│   ├── WorkoutDetailForm.java
│   ├── WorkoutResponse.java
│   ├── WorkoutDetailResponse.java
│   ├── WorkoutSummaryResponse.java
│   ├── WorkoutStatByType.java
│   └── WorkoutMonthlyStat.java
├── exception
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── WorkoutNotFoundException.java
│   └── InvalidPageSizeException.java
├── repository
│   ├── WorkoutRepository.java
│   ├── JpaWorkoutRepository.java
│   ├── JpaWorkoutRepositoryAdapter.java
│   └── MemoryWorkoutRepository.java
└── service
└── WorkoutService.java
src/test/java/com/kdongdexample/norunnolifeexample
├── controller
│   └── WorkoutControllerTest.java
├── domain
│   └── WorkoutTest.java
├── repository
│   └── WorkoutRepositoryTest.java
└── service
└── WorkoutServiceTest.java
---
## 📦 설계 의도
### 1) Controller
- `@RestController`로 JSON 응답
- `@Valid`로 요청 데이터 검증
- `ResponseEntity`로 HTTP 상태코드 명시적 제어 (등록 201, 조회 200)
- `@CrossOrigin`으로 프론트엔드 CORS 허용
- 목록 조회는 `type`, `from`, `to`, `Pageable` 파라미터를 받아 검색·페이징 처리

### 2) Domain
**Workout**
- 정적 팩토리 메서드 `create()`로만 생성 — 객체 생성 방식 통제
- `withId()`는 MemoryWorkoutRepository에서 id 주입 시에만 사용
- `addDetail()`로 WorkoutDetail 추가 — 연관관계 편의 메서드
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA 프록시 생성용, 외부 직접 호출 차단

**WorkoutDetail**
- `@ManyToOne(fetch = FetchType.LAZY)` — 지연 로딩으로 불필요한 쿼리 방지

### 3) DTO
- `record` 타입으로 불변 객체 — 요청 데이터 변경 불필요
- Bean Validation으로 입력값 검증 (`@NotNull`, `@Min`, `@Max`, `@Size`, `@PastOrPresent`)
- `WorkoutSummaryResponse` — 목록 조회 전용 DTO, `details` 제외
  (컬렉션 fetch join + 페이징 조합 시 발생하는 HHH000104 경고를 원천 차단)
- `WorkoutStatByType`, `WorkoutMonthlyStat` — 통계 API용 DTO Projection

### 4) Repository
- 어댑터 패턴 적용 — 서비스가 구현체에 의존하지 않고 `WorkoutRepository` 인터페이스에만 의존
- `JpaWorkoutRepository` — Spring Data JPA, `LEFT JOIN FETCH`로 N+1 해결
- `JpaWorkoutRepositoryAdapter` — JpaWorkoutRepository를 감싸 WorkoutRepository 구현
  - 타입/기간 조건 조합(8가지)에 따라 쿼리 메서드 분기
  - `from`만 있으면 그 이후 전부, `to`만 있으면 그 이전 전부로 처리
- `MemoryWorkoutRepository` — 메모리 저장소, `ConcurrentHashMap` + `AtomicLong`으로 동시성 처리
  - `search()`, `statsByType()`, `statsByMonth()`는 미사용 구현체라 `UnsupportedOperationException` 처리
- 통계는 DTO Projection(`select new ...`)으로 DB GROUP BY 집계 — 애플리케이션 레벨 합산 금지

### 5) Service
- `@Transactional(readOnly = true)` 클래스 레벨 적용 — 조회 성능 최적화
- 쓰기 메서드에만 `@Transactional` 재선언
- 목록 조회 시 페이지 크기 상한(100) 검증

### 6) Exception
- `@RestControllerAdvice`로 전역 예외 처리
- `WorkoutNotFoundException` → 404
- `MethodArgumentNotValidException` → 400 + 필드별 에러 메시지
- `PropertyReferenceException` → 400 (정렬 필드 오류)
- `MethodArgumentTypeMismatchException` → 400 (요청 파라미터 타입 오류)
- `InvalidPageSizeException` → 400 (페이지 크기 초과)
---
## 📡 API 엔드포인트
| Method | URI | 설명 |
|--------|-----|------|
| GET | `/workouts` | 운동 기록 검색 (type, from, to, page, size, sort) |
| POST | `/workouts` | 운동 기록 등록 |
| GET | `/workouts/{id}` | 운동 기록 단건 조회 (details 포함) |
| DELETE | `/workouts/{id}` | 운동 기록 삭제 |
| GET | `/workouts/stats/by-type` | 타입별 통계 (count, 총 운동시간) |
| GET | `/workouts/stats/monthly` | 월별 운동 횟수 통계 |

예시:
GET /workouts?type=BOXING&from=2026-05-01T00:00:00&to=2026-05-31T23:59:59&sort=workoutDateTime,desc&page=0&size=10
---
## ✅ 구현 기능
- 운동 기록 등록 / 단건 조회 / 삭제 API
- 운동 세부 기록 (WorkoutDetail) 1:N 관계 관리
- 서버 사이드 유효성 검증
- N+1 문제 해결 (LEFT JOIN FETCH)
- 전역 예외 처리
- JPA / 메모리 저장소 교체 가능한 어댑터 패턴
- CORS 설정으로 프론트엔드 연동
- 타입·기간 조건 검색 + 정렬 (쿼리 메서드 기반)
- 페이징 처리 (목록 응답에서 details 제외)
- DB 집계 기반 통계 API (타입별 / 월별)
