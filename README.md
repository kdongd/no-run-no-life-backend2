# 🥊 NO RUN NO LIFE
> 기록이 실력을 만든다. 러닝과 복싱 기록을 남기는 웹 서비스.

---

## 📌 프로젝트 소개
- 러닝과 복싱, 두 종목의 기록을 한 곳에서 관리하는 Spring Boot REST API 서버
- Spring MVC, JPA를 단계별로 적용하며 처음부터 직접 만들어가는 프로젝트
- 매주 멘토 리뷰를 받으며 진행한 6주 과정의 결과물

---

## 🛠 기술 스택
| 분류 | 기술                          |
|------|-----------------------------|
| Language | Java 17                     |
| Framework | Spring Boot 3.5             |
| API | REST API, springdoc-openapi (Swagger UI) |
| Build | Gradle                      |
| DB | MySQL (운영) / MySQL Testcontainers, H2 (테스트) |
| ORM | Spring Data JPA / Hibernate |
| CI | GitHub Actions               |

---

## ⚙️ 실행 방법

로컬에 MySQL이 떠 있어야 하고, `norunnolife` 데이터베이스와 접속 계정이 필요합니다. `spring.datasource.username`/`password`는 `application.yml`에서 환경변수 `DB_USERNAME`/`DB_PASSWORD`로 주입받으므로, 실행 전에 값을 설정해야 합니다.

    export DB_USERNAME=your_db_user
    export DB_PASSWORD=your_db_password
    ./gradlew bootRun

서버 실행 후 `http://localhost:8080` 에서 API 사용 가능
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

H2 콘솔(`http://localhost:8080/h2-console`)도 활성화되어 있지만, 실제 데이터소스는 MySQL입니다. 콘솔에서 뭔가 조회하려면 접속 화면에서 JDBC URL을 MySQL 접속 정보(`jdbc:mysql://localhost:3306/norunnolife`)로 직접 바꿔 입력해야 하며, 기본값(H2 임베디드 URL) 그대로는 앱 데이터가 보이지 않습니다.

---

## 📁 프로젝트 구조

    src/main/java/com/kdongdexample/norunnolifeexample
    ├── config
    │   ├── WebConfig.java
    │   └── AuditingConfig.java
    ├── controller
    │   └── WorkoutController.java
    ├── domain
    │   ├── Workout.java
    │   ├── RunningWorkout.java
    │   ├── BoxingWorkout.java
    │   ├── WorkoutDetail.java
    │   ├── WorkoutType.java
    │   └── TechniqueType.java
    ├── dto
    │   ├── WorkoutForm.java
    │   ├── WorkoutDetailForm.java
    │   ├── WorkoutResponse.java
    │   ├── WorkoutDetailResponse.java
    │   ├── WorkoutSummaryResponse.java
    │   ├── WorkoutTypeFields.java
    │   ├── PageResponse.java
    │   ├── WorkoutStatByType.java
    │   └── WorkoutMonthlyStat.java
    ├── exception
    │   ├── ErrorResponse.java
    │   ├── GlobalExceptionHandler.java
    │   ├── WorkoutNotFoundException.java
    │   ├── InvalidSortPropertyException.java
    │   └── WorkoutTypeMismatchException.java
    ├── repository
    │   ├── WorkoutRepository.java
    │   ├── WorkoutQueryRepository.java
    │   ├── JpaWorkoutRepository.java
    │   ├── JpaWorkoutRepositoryAdapter.java
    │   └── WorkoutSpecifications.java
    └── service
        └── WorkoutService.java

    src/test/java/com/kdongdexample/norunnolifeexample
    ├── controller
    │   └── WorkoutControllerTest.java
    ├── domain
    │   ├── WorkoutTest.java
    │   └── WorkoutAuditingTest.java
    ├── repository
    │   ├── WorkoutRepositoryTest.java
    │   ├── JpaWorkoutRepositoryAdapterTest.java
    │   └── JpaWorkoutRepositoryMySQLTest.java
    └── service
        └── WorkoutServiceTest.java

    .github/workflows/ci.yml

---

## 📦 설계 의도

### 1) Controller
- `@RestController`로 JSON 응답
- `@Valid`로 요청 데이터 검증
- `ResponseEntity`로 HTTP 상태코드 명시적 제어 (등록 201 + `Location` 헤더, 조회/수정 200, 삭제 204)
- CORS는 `WebConfig`(`WebMvcConfigurer`)에서 전역으로 처리, origin은 `application.yml`의 `cors.allowed-origins`로 설정화
  (`@CrossOrigin` 어노테이션 속성에서는 SpEL 기반 동적 분리가 실제로 동작하지 않는 것을 확인하여 이 방식으로 변경)
- 목록 조회는 `type`, `from`, `to`, `Pageable` 파라미터를 받아 검색·페이징 처리
- 수정은 `PUT /workouts/{id}`로 처리 — 리소스 전체 교체
- 통계 API(`stats/by-type`, `stats/monthly`)도 `from`/`to` 기간 필터 지원 (미지정 시 전체 집계)
- `@Tag`, `@Operation`, `@Parameter`, `@ApiResponses`로 Swagger 문서 자동 생성 (springdoc-openapi)

### 2) Domain
**Workout / RunningWorkout / BoxingWorkout**
- `Workout`은 `abstract` 클래스, `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` + `@DiscriminatorColumn(name = "dtype")` 적용
- `RunningWorkout`(distanceKm, place, caloriesBurned), `BoxingWorkout`(rounds, sparringPartner, techniqueType)이 `Workout`을 상속
- 정적 팩토리 메서드 `create()`로만 생성 — 객체 생성 방식 통제 (생성자는 private)
- `update()`로 같은 타입 내 필드 전체 교체 — 타입 자체는 변경 불가(요청 타입이 기존과 다르면 `WorkoutTypeMismatchException`)
- `addDetail()` / `clearDetails()`로 WorkoutDetail 추가·전체 교체 — 연관관계 편의 메서드
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA 프록시 생성용, 외부 직접 호출 차단
- `@CreatedDate`/`@LastModifiedDate` + `@EntityListeners(AuditingEntityListener.class)`로 생성/수정 시각 자동 관리 (`AuditingConfig`의 `@EnableJpaAuditing`으로 활성화)
- id는 `@GeneratedValue(strategy = GenerationType.IDENTITY)`로 DB가 채번 (별도 id 할당기는 없음)

**WorkoutDetail**
- `@ManyToOne(fetch = FetchType.LAZY)` — 지연 로딩으로 불필요한 쿼리 방지

### 3) DTO
- `record` 타입으로 불변 객체 — 요청 데이터 변경 불필요
- Bean Validation으로 입력값 검증 (`@NotNull`, `@Min`, `@Max`, `@Size`, `@PastOrPresent`, `@Positive`)
- `WorkoutForm`에 러닝·복싱 전용 필드를 모두 두고, `@AssertTrue` 커스텀 검증 4종으로 타입-필드 정합성을 양방향 체크
  - 러닝인데 `distanceKm` 없으면 400 / 복싱인데 `rounds` 없으면 400
  - 복싱인데 러닝 전용 필드(`distanceKm`/`place`/`caloriesBurned`)를 보내도 400 / 러닝인데 복싱 전용 필드(`rounds`/`sparringPartner`/`techniqueType`)를 보내도 400
  - 등록/수정 폼이 필드·검증 로직이 100% 동일해 `WorkoutForm` 하나로 통합, 타입 불변성(수정 시 타입 변경 금지)은 서비스 계층에서 처리
- `WorkoutResponse`/`WorkoutSummaryResponse`는 `WorkoutTypeFields`로 `instanceof` 분기를 공통화해 타입별 필드를 노출
- `WorkoutSummaryResponse` — 목록 조회 전용 DTO, `details` 제외
  (컬렉션 fetch join + 페이징 조합 시 발생하는 HHH000104 경고를 원천 차단)
- `PageResponse<T>` — Spring `Page`를 그대로 직렬화하지 않고 `content`/`number`/`size`/`totalElements`/`totalPages`/`first`/`last`만 감싼 응답
- `WorkoutStatByType`, `WorkoutMonthlyStat` — 통계 API용 DTO Projection
- `@Schema`로 필드 설명·예시 값 부여 (Swagger 문서화)

### 4) Repository
- 인터페이스 분리 — `WorkoutRepository`(CRUD 전용: save/findById/delete)와
  `WorkoutQueryRepository`(검색/통계: search/statsByType/statsByMonth)를 분리
  - 서비스는 두 인터페이스에만 의존하는 구조라 구현체 교체가 가능하지만, 현재 구현체는 `JpaWorkoutRepositoryAdapter` 하나뿐입니다(`@Primary`는 지금 시점에는 별 의미 없이 붙어 있는 상태).
- `JpaWorkoutRepository` — Spring Data JPA, `JpaSpecificationExecutor` 상속
  - 단건 조회(`findByIdWithDetails`)는 `LEFT JOIN FETCH`로 N+1 해결
  - 목록 검색은 `WorkoutSpecifications`로 동적 조건을 조합해 처리 (파생 쿼리 메서드 방식 대신 Specification 사용)
  - 통계 쿼리는 `from`/`to` 널 허용 조건(`:from is null or ...`)으로 기간 필터를 옵션 처리
- `JpaWorkoutRepositoryAdapter` — `WorkoutRepository`, `WorkoutQueryRepository` 모두 구현
  - `search()`는 type/from/to 존재 여부에 따라 `WorkoutSpecifications`의 조건들을 동적으로 조합
  - `from`/`to` 경계는 둘 다 포함(`greaterThanOrEqualTo`/`lessThanOrEqualTo`)으로 통일
  - `distanceKm` 정렬은 `cb.treat()`로 서브클래스(RunningWorkout) 필드에 접근, `CASE WHEN` 표현식으로 NULLS LAST 처리 (다른 타입 레코드는 정렬 결과 뒤로)
- 통계는 DTO Projection(`select new ...`)으로 DB GROUP BY 집계 — 애플리케이션 레벨 합산 금지
  - `statsByType()`은 `coalesce(sum(...), 0L)`로 합계가 null이 되는 경우를 방어
  - `statsByMonth()`은 `extract(year/month from ...)`를 사용해 DB 방언 종속성 제거

### 5) Service
- `@Transactional(readOnly = true)` 클래스 레벨 적용 — 조회 성능 최적화
- 쓰기 메서드에만 `@Transactional` 재선언
- 목록 조회 시 정렬 필드 화이트리스트 검증 (`workoutDateTime`, `durationMinutes`, `type`, `distanceKm`만 허용)
- 페이지 크기 상한은 `spring.data.web.pageable.max-page-size`(100) 설정으로 초과분을 자동 조정
- `update()`는 기존 엔티티를 조회해 같은 타입인지 확인 후 필드 갱신, details는 전체 삭제 후 재생성

### 6) Exception
- `@RestControllerAdvice`로 전역 예외 처리
- `WorkoutNotFoundException` → 404
- `MethodArgumentNotValidException` → 400 + 필드별 에러 메시지
- `MethodArgumentTypeMismatchException` → 400 (요청 파라미터 타입 오류)
- `InvalidSortPropertyException` → 400 (화이트리스트에 없는 정렬 필드 요청)
- `WorkoutTypeMismatchException` → 400 (수정 시 타입 변경 시도)

### 7) API 문서화 (Swagger)
- `springdoc-openapi-starter-webmvc-ui`로 OpenAPI 3.1 스펙 자동 생성
- `@Tag`/`@Operation`/`@Parameter`로 엔드포인트 설명, `@Schema`로 DTO 필드 설명·예시 값 부여
- 400/404 에러 응답을 `@ApiResponses` + `ErrorResponse` 스키마로 연결

### 8) CI (GitHub Actions)
- PR 생성·main push 시 `./gradlew build` 자동 실행 (JDK 17, Testcontainers MySQL 포함)
- main 브랜치 보호 규칙 적용 — CI(`build`) 통과 없이는 병합 불가

---

## 📡 API 엔드포인트
| Method | URI | 설명 |
|--------|-----|------|
| GET | `/workouts` | 운동 기록 검색 (type, from, to, page, size, sort) → `PageResponse` |
| POST | `/workouts` | 운동 기록 등록 → 201 + `Location` 헤더 |
| GET | `/workouts/{id}` | 운동 기록 단건 조회 (details 포함) |
| PUT | `/workouts/{id}` | 운동 기록 수정 (타입 변경 불가) |
| DELETE | `/workouts/{id}` | 운동 기록 삭제 |
| GET | `/workouts/stats/by-type` | 타입별 통계 (count, 총 운동시간), from/to 기간 필터 가능 |
| GET | `/workouts/stats/monthly` | 월별 운동 횟수 통계, from/to 기간 필터 가능 |

예시:

    GET /workouts?type=BOXING&from=2026-05-01T00:00:00&to=2026-05-31T23:59:59&sort=distanceKm,desc&page=0&size=10
    GET /workouts/stats/by-type?from=2026-01-01T00:00:00&to=2026-06-30T23:59:59

전체 스펙은 Swagger UI(`/swagger-ui/index.html`) 참고.

---

## ✅ 구현 기능
- 운동 기록 등록 / 단건 조회 / 수정 / 삭제 API
- 러닝/복싱 타입별 속성 분화 (JPA SINGLE_TABLE 상속) 및 타입-필드 정합성 검증(양방향)
- 운동 세부 기록 (WorkoutDetail) 1:N 관계 관리, 수정 시 전체 교체
- 서버 사이드 유효성 검증
- 단건 조회 N+1 문제 해결 (LEFT JOIN FETCH)
- 전역 예외 처리
- CRUD/Query 인터페이스 분리로 구현체 교체가 가능한 구조 (현재 구현체는 JPA 어댑터 하나)
- CORS 설정으로 프론트엔드 연동 (설정 파일로 externalize)
- 타입·기간 조건 검색 + 정렬 (Specification 기반, 경계 정책 통일), 타입 전용 필드 정렬 시 NULLS LAST 처리
- 정렬 필드 화이트리스트 검증
- 페이징 처리 (`PageResponse`로 감싼 응답, 목록 조회에서 details 제외, 상한 100건 자동 조정)
- DB 집계 기반 통계 API (타입별 / 월별), 기간 필터(from/to), null 합계 방어 및 DB 방언 이식성 확보
- JPA Auditing (createdAt, updatedAt 자동 관리)
- Swagger(OpenAPI) API 문서 자동 생성
- GitHub Actions CI + main 브랜치 보호
