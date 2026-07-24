# 🥊 NO RUN NO LIFE
> 기록이 실력을 만든다. 러닝과 복싱 기록을 남기는 웹 서비스.
---
## 📌 프로젝트 소개
- 러닝과 복싱, 두 종목의 기록을 한 곳에서 관리하는 Spring Boot REST API 서버
- Spring MVC, JPA를 단계별로 적용하며 처음부터 직접 만들어가는 프로젝트
---
## 🛠 기술 스택
| 분류 | 기술                          |
|------|-----------------------------|
| Language | Java 17                     |
| Framework | Spring Boot 3.5             |
| API | REST API                    |
| Build | Gradle                      |
| DB | H2 (In-Memory)              |
| ORM | Spring Data JPA / Hibernate |
---
## ⚙️ 실행 방법

    ./gradlew bootRun

서버 실행 후 `http://localhost:8080` 에서 API 사용 가능  
H2 콘솔: `http://localhost:8080/h2-console`
---
## 📁 프로젝트 구조

    src/main/java/com/kdongdexample/norunnolifeexample
    ├── config
    │   └── WebConfig.java
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
    │   └── InvalidSortPropertyException.java
    ├── repository
    │   ├── WorkoutRepository.java
    │   ├── WorkoutQueryRepository.java
    │   ├── JpaWorkoutRepository.java
    │   ├── JpaWorkoutRepositoryAdapter.java
    │   ├── WorkoutSpecifications.java
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
- CORS는 `WebConfig`(`WebMvcConfigurer`)에서 전역으로 처리, origin은 `application.yml`의 `cors.allowed-origins`로 설정화
  (`@CrossOrigin` 어노테이션 속성에서는 SpEL 기반 동적 분리가 실제로 동작하지 않는 것을 확인하여 이 방식으로 변경)
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
- 인터페이스 분리 — `WorkoutRepository`(CRUD 전용: save/findById/delete)와
  `WorkoutQueryRepository`(검색/통계: search/statsByType/statsByMonth)를 분리
  - 서비스는 두 인터페이스에만 의존, 구현체 교체 가능
- `JpaWorkoutRepository` — Spring Data JPA, `JpaSpecificationExecutor` 상속
  - 단건 조회(`findByIdWithDetails`)는 `LEFT JOIN FETCH`로 N+1 해결
  - 목록 검색은 `WorkoutSpecifications`로 동적 조건을 조합해 처리 (파생 쿼리 메서드 방식 대신 Specification 사용)
- `JpaWorkoutRepositoryAdapter` — `WorkoutRepository`, `WorkoutQueryRepository` 모두 구현
  - `search()`는 type/from/to 존재 여부에 따라 `WorkoutSpecifications`의 조건들을 동적으로 조합
  - `from`/`to` 경계는 둘 다 포함(`greaterThanOrEqualTo`/`lessThanOrEqualTo`)으로 통일
- `MemoryWorkoutRepository` — 메모리 저장소, `ConcurrentHashMap` + `AtomicLong`으로 동시성 처리
  - `WorkoutRepository`(CRUD)만 구현, 검색/통계 기능은 제공하지 않음
- 통계는 DTO Projection(`select new ...`)으로 DB GROUP BY 집계 — 애플리케이션 레벨 합산 금지
  - `statsByType()`은 `coalesce(sum(...), 0L)`로 합계가 null이 되는 경우를 방어
  - `statsByMonth()`은 `extract(year/month from ...)`를 사용해 DB 방언 종속성 제거

### 5) Service
- `@Transactional(readOnly = true)` 클래스 레벨 적용 — 조회 성능 최적화
- 쓰기 메서드에만 `@Transactional` 재선언
- 목록 조회 시 정렬 필드 화이트리스트 검증 (`workoutDateTime`, `durationMinutes`, `type`만 허용)
- 페이지 크기 상한은 `spring.data.web.pageable.max-page-size`(100) 설정으로 초과분을 자동 조정

### 6) Exception
- `@RestControllerAdvice`로 전역 예외 처리
- `WorkoutNotFoundException` → 404
- `MethodArgumentNotValidException` → 400 + 필드별 에러 메시지
- `MethodArgumentTypeMismatchException` → 400 (요청 파라미터 타입 오류)
- `InvalidSortPropertyException` → 400 (화이트리스트에 없는 정렬 필드 요청)
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
- 단건 조회 N+1 문제 해결 (LEFT JOIN FETCH)
- 전역 예외 처리
- JPA / 메모리 저장소 교체 가능한 어댑터 패턴 + CRUD/Query 인터페이스 분리
- CORS 설정으로 프론트엔드 연동 (설정 파일로 externalize)
- 타입·기간 조건 검색 + 정렬 (Specification 기반, 경계 정책 통일)
- 정렬 필드 화이트리스트 검증
- 페이징 처리 (목록 응답에서 details 제외, 상한 100건 자동 조정)
- DB 집계 기반 통계 API (타입별 / 월별), null 합계 방어 및 DB 방언 이식성 확보

---

## 🔧 settings.gradle 누락

`settings.gradle`은 PR #1(1-3주차, 최초 구현)부터 지금까지 커밋 이력에 없었습니다. 프로젝트 폴더가 iCloud 동기화 폴더로 옮겨졌다가 복구한 적이 있어서 그때 유실됐나 싶었는데, PR #1의 최초 커밋부터 이미 없었던 걸로 봐서 그보다는 애초에 생성을 안 했다고 생각합니다.

말씀하신 대로 새 폴더에 다른 이름(`test-name-check`)으로 클론해서 `./gradlew build` 해봤는데, `rootProject.name`이 `norunnolifeexample`이 아니라 클론 디렉터리명인 `test-name-check`로 잡혔습니다. `settings.gradle`이 없으면 Gradle이 루트 디렉터리 이름으로 프로젝트명을 자동 추론한다는 걸 직접 확인했습니다.

`settings.gradle`을 다시 추가하는 과정에서 로컬 환경 문제를 하나 더 발견했습니다. 추가 후 빌드가 `Unsupported class file major version 68`로 실패했는데, 확인해보니 시스템 기본 `java`가 어느샌가 24로 바뀌어 있었고 Gradle 8.10은 Java 23까지만 지원해서 생긴 문제였습니다. `gradle.properties`에 `org.gradle.java.home`을 JDK 17 경로로 지정해서 고정했는데, 이 경로가 제 로컬 절대경로라 커밋하면 다른 환경에서 깨질 걸 뒤늦게 알아차려서 `.gitignore`로 옮겼습니다.

`settings.gradle`은 다시 추가해서 `rootProject.name = 'norunnolifeexample'`로 고정했습니다(`application.yml`의 `spring.application.name`과 동일하게 맞췄습니다). 깨끗한 폴더에 새로 클론해서 `settings.gradle`은 있고 `gradle.properties`는 없는 것까지 확인했습니다. 푸시 완료했습니다.

---

## 솔직 리뷰

 - 일단 리뷰해주신 것들을 먼저 생각해보고 어떤 방법으로 접근할지에 대해서 고민을 합니다.
 - 근데 아직 기본 이론이 부족한지 스스로의 해결방법으로 해결 한 것은 별로 없었습니다.
 - 대부분 AI나 검색 위주로 해결방안을 얻고 이해하기까지 공부한 뒤 코드 수정을 했습니다.
 - 그리고 이런 방식으로 해결하고나서 다음에 같은 문제가 있을때는 꼭 적용해볼려고 합니다. 
