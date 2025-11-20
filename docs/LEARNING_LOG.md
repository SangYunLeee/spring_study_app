# Spring 학습 진행 상황

이 파일은 Claude Code와 함께 학습한 내용을 기록합니다.

## 학습 세션 기록

### 세션 1: Spring Boot 프로젝트 초기 설정 (2025-11-06)

#### 학습 목표
- Spring Boot 프로젝트 생성 및 실행
- 기본 REST API 컨트롤러 작성
- HTTP 요청 처리 방법 이해

#### 완료한 작업

1. **프로젝트 초기 설정**
   - ✅ Gradle 프로젝트 생성
   - ✅ Spring Boot 3.2.0 설정
   - ✅ Java 21 환경 설정
   - ✅ 프로젝트 구조 생성

2. **Spring Boot 메인 애플리케이션 작성**
   - 파일: [SpringBasicApplication.java](src/main/java/com/example/springbasic/SpringBasicApplication.java)
   - `@SpringBootApplication` 어노테이션 이해
   - Spring Boot 애플리케이션의 시작점 학습

3. **첫 번째 REST 컨트롤러 작성**
   - 파일: [HelloController.java](src/main/java/com/example/springbasic/controller/HelloController.java)
   - `@RestController` 어노테이션 이해
   - 4가지 엔드포인트 구현:
     - `GET /hello` - 기본 문자열 반환
     - `GET /greet?name=xxx` - 쿼리 파라미터 사용
     - `GET /welcome/{name}` - 경로 변수 사용
     - `GET /info` - JSON 객체 반환

4. **애플리케이션 설정**
   - 파일: [application.properties](src/main/resources/application.properties)
   - 서버 포트 설정 (8080)
   - 로그 레벨 설정

5. **애플리케이션 실행 및 테스트**
   - ✅ `./gradlew bootRun`으로 실행 성공
   - ✅ 모든 엔드포인트 테스트 완료
   - ✅ curl 명령어로 API 호출 확인

#### 학습한 핵심 개념

##### 1. Spring Boot의 3대 핵심 어노테이션
```java
@SpringBootApplication  // = @Configuration + @EnableAutoConfiguration + @ComponentScan
@RestController        // = @Controller + @ResponseBody
@GetMapping("/path")   // HTTP GET 요청 매핑
```

##### 2. HTTP 파라미터 전달 방법

**쿼리 파라미터** - `@RequestParam`
```java
@GetMapping("/greet")
public String greet(@RequestParam(defaultValue = "Guest") String name) {
    return "안녕하세요, " + name + "님!";
}
// 사용: /greet?name=철수
```

**경로 변수** - `@PathVariable`
```java
@GetMapping("/welcome/{name}")
public String welcome(@PathVariable String name) {
    return "환영합니다, " + name + "님!";
}
// 사용: /welcome/영희
```

##### 3. JSON 자동 변환
- `@RestController`를 사용하면 반환값이 자동으로 JSON으로 변환됨
- Java 객체(record, class) → JSON 자동 변환

##### 4. 프로젝트 구조의 의미
```
src/main/java           # Java 소스 코드
  └── controller/       # HTTP 요청을 받는 컨트롤러 (앞으로 service, repository 등 추가 예정)
src/main/resources      # 설정 파일 및 정적 리소스
```

#### 테스트 결과

| 엔드포인트 | 요청 예시 | 응답 | 상태 |
|-----------|---------|------|------|
| `/hello` | `curl http://localhost:8080/hello` | `Hello, Spring!` | ✅ |
| `/greet` | `curl http://localhost:8080/greet?name=철수` | `안녕하세요, 철수님!` | ✅ |
| `/welcome/{name}` | `curl http://localhost:8080/welcome/영희` | `환영합니다, 영희님!` | ✅ |
| `/info` | `curl http://localhost:8080/info` | `{"message":"Hello","timestamp":...}` | ✅ |

#### 문제 해결 경험

1. **Gradle Wrapper 없음**
   - 문제: `./gradlew` 명령어 실행 불가
   - 해결: `gradle wrapper` 명령어로 생성

2. **한글 쿼리 파라미터**
   - 문제: 한글 파라미터가 URL에 직접 입력 시 오류
   - 해결: URL 인코딩 사용 (`%EC%B2%A0%EC%88%98`)

#### 다음에 학습할 내용

다음 세션에서는 아래 주제 중 하나를 선택하여 학습할 예정입니다:

1. **Service 레이어** - 비즈니스 로직 분리
2. **데이터베이스 연동** - JPA와 H2 데이터베이스
3. **다양한 HTTP 메서드** - POST, PUT, DELETE
4. **예외 처리** - @ExceptionHandler, @ControllerAdvice
5. **유효성 검증** - @Valid, @NotNull 등

자세한 내용은 [NEXT_STEPS.md](NEXT_STEPS.md) 참고

---

### 세션 2: Service 레이어와 계층 구조 (2025-11-06)

#### 학습 목표
- Controller-Service-Repository 3계층 구조 이해
- 의존성 주입(Dependency Injection) 마스터
- 비즈니스 로직 분리 방법 학습

#### 완료한 작업

1. **계산기 서비스 구현**
   - 파일: [CalculatorService.java](src/main/java/com/example/springbasic/service/CalculatorService.java)
   - 파일: [CalculatorController.java](src/main/java/com/example/springbasic/controller/CalculatorController.java)
   - `@Service` 어노테이션 이해
   - 생성자 주입 방식 학습

2. **사용자 관리 시스템 구현 (3계층 구조)**
   - Model: [User.java](src/main/java/com/example/springbasic/model/User.java)
     - Java record 사용 (불변 데이터 클래스)
     - Compact constructor로 유효성 검증
   - Repository: [UserRepository.java](src/main/java/com/example/springbasic/repository/UserRepository.java)
     - `@Repository` 어노테이션
     - In-Memory 저장소 구현 (ConcurrentHashMap)
     - CRUD 기본 연산 구현
   - Service: [UserService.java](src/main/java/com/example/springbasic/service/UserService.java)
     - 비즈니스 로직 (중복 이메일 체크, 통계 계산 등)
     - 생성자 주입으로 Repository 사용
   - Controller: [UserController.java](src/main/java/com/example/springbasic/controller/UserController.java)
     - ResponseEntity 사용 (HTTP 상태 코드)
     - 생성자 주입으로 Service 사용

3. **의존성 주입 학습 자료 작성**
   - 파일: [DependencyInjectionExamples.java](src/main/java/com/example/springbasic/examples/DependencyInjectionExamples.java)
   - 3가지 주입 방식 비교 (생성자, 필드, Setter)
   - 각 방식의 장단점 정리

4. **계층 구조 문서 작성**
   - 파일: [LAYER_ARCHITECTURE.md](LAYER_ARCHITECTURE.md)
   - 계층별 역할 정리
   - 실습 예제 및 테스트 방법

#### 학습한 핵심 개념

##### 1. 3계층 구조 (Layered Architecture)

```
Controller (HTTP 계층)
    ↓ 의존성 주입
Service (비즈니스 로직)
    ↓ 의존성 주입
Repository (데이터 접근)
```

**각 계층의 역할:**
- **Controller**: HTTP 요청/응답만 처리
- **Service**: 비즈니스 로직 (검증, 계산, 변환)
- **Repository**: 데이터 저장/조회

##### 2. 의존성 주입 (Dependency Injection)

**생성자 주입 (권장):**
```java
@Service
public class UserService {
    private final UserRepository repository;  // final 가능!

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

**장점:**
- ✅ final 사용 가능 (불변성)
- ✅ 필수 의존성 보장
- ✅ 테스트 용이
- ✅ 순환 참조 방지

##### 3. Spring의 주요 어노테이션

| 어노테이션 | 용도 | 계층 |
|----------|------|------|
| `@RestController` | REST API 컨트롤러 | Controller |
| `@Service` | 비즈니스 로직 서비스 | Service |
| `@Repository` | 데이터 접근 계층 | Repository |
| `@Component` | 일반 Spring Bean | 공통 |

##### 4. ResponseEntity 사용

HTTP 상태 코드를 명시적으로 반환:
```java
// 200 OK
return ResponseEntity.ok(user);

// 201 Created
return ResponseEntity.status(HttpStatus.CREATED).body(user);

// 404 Not Found
return ResponseEntity.notFound().build();

// 204 No Content
return ResponseEntity.noContent().build();
```

##### 5. Optional 활용

null 대신 Optional 사용:
```java
public Optional<User> findById(Long id) {
    return Optional.ofNullable(storage.get(id));
}

// 사용
return userService.getUserById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
```

#### 테스트 결과

**계산기 API:**
| 엔드포인트 | 테스트 | 결과 |
|-----------|--------|------|
| `/calculator/add?a=10&b=5` | 덧셈 | `{"operation":"addition","a":10,"b":5,"result":15.0}` ✅ |
| `/calculator/multiply?a=7&b=8` | 곱셈 | `{"operation":"multiplication","a":7,"b":8,"result":56.0}` ✅ |
| `/calculator/average?numbers=10,20,30,40,50` | 평균 | `{"average":30.0,"count":5}` ✅ |

**사용자 관리 API:**
| 엔드포인트 | 테스트 | 결과 |
|-----------|--------|------|
| `/api/users/create?name=홍길동&email=hong@example.com&age=25` | 사용자 생성 | `{"id":1,"name":"홍길동",...}` ✅ |
| `/api/users` | 전체 조회 | 3명의 사용자 배열 ✅ |
| `/api/users/1` | ID로 조회 | 홍길동 정보 ✅ |
| `/api/users/adults` | 성인만 조회 | 2명 (19세 이상) ✅ |
| `/api/users/statistics` | 통계 | `{"totalCount":3,"adultCount":2,"averageAge":24.0,...}` ✅ |
| `/api/users/search?keyword=홍` | 이름 검색 | 홍길동 1명 ✅ |

#### 비즈니스 로직 예제

Service에서 구현한 비즈니스 로직:

1. **중복 검증**
   ```java
   if (userRepository.findByEmail(email).isPresent()) {
       throw new IllegalArgumentException("이미 존재하는 이메일입니다");
   }
   ```

2. **통계 계산**
   ```java
   double averageAge = allUsers.stream()
           .mapToInt(User::age)
           .average()
           .orElse(0.0);
   ```

3. **필터링**
   ```java
   public List<User> getAdultUsers() {
       return userRepository.findAll().stream()
               .filter(user -> user.age() >= 19)
               .toList();
   }
   ```

#### 프로젝트 구조 변화

```
src/main/java/com/example/springbasic/
├── SpringBasicApplication.java
├── controller/
│   ├── HelloController.java          (기존)
│   ├── CalculatorController.java     (신규)
│   └── UserController.java           (신규)
├── service/
│   ├── CalculatorService.java        (신규)
│   └── UserService.java              (신규)
├── repository/
│   └── UserRepository.java           (신규)
├── model/
│   └── User.java                     (신규)
└── examples/
    └── DependencyInjectionExamples.java (신규)
```

#### 핵심 깨달음

1. **계층 분리의 중요성**
   - 각 계층이 자신의 역할에만 집중
   - 유지보수가 쉬워짐
   - 테스트가 쉬워짐

2. **생성자 주입만 사용하기**
   - 필드 주입은 사용하지 말 것
   - final로 불변성 보장

3. **비즈니스 로직은 Service에**
   - Controller는 HTTP만 처리
   - Repository는 데이터 접근만

4. **Java의 현대적 기능 활용**
   - record (불변 데이터 클래스)
   - Optional (null 안전성)
   - Stream API (함수형 프로그래밍)

#### 다음 학습 주제

레벨 2를 완료했습니다! 다음 단계 추천:

1. **데이터베이스 연동 (JPA)** - 메모리 대신 실제 DB 사용
2. **POST/PUT/DELETE 메서드** - RESTful API 완성
3. **예외 처리** - 전역 예외 처리기

---

### 세션 3: RESTful API와 HTTP 메서드 (2025-11-06)

#### 학습 목표
- POST, PUT, PATCH, DELETE 메서드 마스터
- @RequestBody로 JSON 데이터 받기
- DTO (Data Transfer Object) 패턴 이해
- 완전한 RESTful API 구현

#### 완료한 작업

1. **RESTful API 개념 문서 작성**
   - 파일: [RESTFUL_API.md](RESTFUL_API.md)
   - HTTP 메서드별 용도 정리
   - RESTful URI 설계 원칙
   - HTTP 상태 코드 사용법

2. **Request/Response DTO 구현**
   - [CreateUserRequest.java](src/main/java/com/example/springbasic/dto/CreateUserRequest.java) - POST 요청용
   - [UpdateUserRequest.java](src/main/java/com/example/springbasic/dto/UpdateUserRequest.java) - PUT 요청용
   - [PatchUserRequest.java](src/main/java/com/example/springbasic/dto/PatchUserRequest.java) - PATCH 요청용
   - [UserResponse.java](src/main/java/com/example/springbasic/dto/UserResponse.java) - 응답용

3. **UserService 개선**
   - `patchUser()` 메서드 추가 (부분 수정 로직)
   - null 체크를 통한 선택적 필드 업데이트

4. **UserController 완전 RESTful하게 재구현**
   - 파일: [UserController.java](src/main/java/com/example/springbasic/controller/UserController.java)
   - ✅ POST `/api/users` - 사용자 생성 (201 Created)
   - ✅ GET `/api/users` - 전체 조회 (200 OK)
   - ✅ GET `/api/users/{id}` - ID로 조회 (200, 404)
   - ✅ GET `/api/users/search?keyword=xxx` - 검색
   - ✅ PUT `/api/users/{id}` - 전체 수정 (200)
   - ✅ PATCH `/api/users/{id}` - 부분 수정 (200)
   - ✅ DELETE `/api/users/{id}` - 삭제 (204 No Content)

5. **API 테스트 가이드 작성**
   - 파일: [API_TEST_GUIDE.md](API_TEST_GUIDE.md)
   - 모든 엔드포인트 테스트 방법
   - curl 명령어 예제
   - 실전 테스트 시나리오

#### 학습한 핵심 개념

##### 1. HTTP 메서드와 CRUD 매핑

| HTTP 메서드 | CRUD | 용도 | 멱등성 | 응답 코드 |
|------------|------|------|--------|----------|
| POST | Create | 생성 | ❌ | 201 Created |
| GET | Read | 조회 | ✅ | 200 OK |
| PUT | Update | 전체 수정 | ✅ | 200 OK |
| PATCH | Update | 부분 수정 | ❌ | 200 OK |
| DELETE | Delete | 삭제 | ✅ | 204 No Content |

**멱등성 (Idempotent)**: 같은 요청을 여러 번 해도 결과가 같음

##### 2. @RequestBody - JSON을 Java 객체로 변환

```java
@PostMapping
public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    // JSON → CreateUserRequest 객체로 자동 변환
    User user = userService.createUser(
        request.name(),
        request.email(),
        request.age()
    );
    return ResponseEntity.created(...).body(response);
}
```

##### 3. DTO (Data Transfer Object) 패턴

**왜 DTO를 사용하나요?**
1. Entity 직접 노출 방지 (보안)
2. 필요한 필드만 정의
3. API 변경 시 Entity 영향 최소화

```java
// 요청 DTO - ID 없음 (서버에서 생성)
public record CreateUserRequest(
    String name,
    String email,
    int age
) {}

// 응답 DTO - 민감한 정보 제외
public record UserResponse(
    Long id,
    String name,
    String email,
    int age
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.name(), ...);
    }
}
```

##### 4. PUT vs PATCH 차이

**PUT - 전체 수정**
- 모든 필드 필수
- 리소스 전체를 교체
- 멱등성 보장

```java
@PutMapping("/{id}")
public ResponseEntity<UserResponse> updateUser(
    @PathVariable Long id,
    @RequestBody UpdateUserRequest request  // 모든 필드 필요
) { ... }
```

**PATCH - 부분 수정**
- 변경할 필드만 제공
- null이 아닌 필드만 업데이트

```java
@PatchMapping("/{id}")
public ResponseEntity<UserResponse> patchUser(
    @PathVariable Long id,
    @RequestBody PatchUserRequest request  // 선택적 필드
) {
    // null 체크를 통한 부분 업데이트
    String newName = request.name() != null ? request.name() : existing.name();
    ...
}
```

##### 5. HTTP 상태 코드 적절하게 사용

```java
// 201 Created + Location 헤더
return ResponseEntity
    .created(URI.create("/api/users/" + user.id()))
    .body(response);

// 204 No Content (삭제 성공)
return ResponseEntity.noContent().build();

// 404 Not Found
return ResponseEntity.notFound().build();

// 400 Bad Request
return ResponseEntity.badRequest().build();
```

##### 6. RESTful URI 설계 원칙

```
✅ 좋은 예:
GET    /api/users           # 전체 조회
POST   /api/users           # 생성
GET    /api/users/1         # 조회
PUT    /api/users/1         # 수정
DELETE /api/users/1         # 삭제

❌ 나쁜 예:
GET  /api/getUsers          # 동사 사용 금지
POST /api/createUser        # 동사 사용 금지
GET  /api/users/delete/1    # DELETE 메서드 사용해야 함
```

#### 테스트 결과

**POST - 사용자 생성**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com","age":25}'
```
→ 201 Created, Location 헤더 포함 ✅

**GET - 전체 조회**
```bash
curl http://localhost:8080/api/users
```
→ 200 OK, 배열 반환 ✅

**PUT - 전체 수정**
```bash
curl -X PUT http://localhost:8080/api/users/2 \
  -H "Content-Type: application/json" \
  -d '{"name":"김철수_수정","email":"kim2@example.com","age":31}'
```
→ 200 OK, 수정된 데이터 반환 ✅

**PATCH - 부분 수정** (나이만 변경)
```bash
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"age":26}'
```
→ 200 OK, 나이만 변경됨 ✅

**DELETE - 삭제**
```bash
curl -X DELETE http://localhost:8080/api/users/3
```
→ 204 No Content ✅

#### 프로젝트 구조 변화

```
src/main/java/com/example/springbasic/
├── SpringBasicApplication.java
├── controller/
│   ├── HelloController.java
│   ├── CalculatorController.java
│   └── UserController.java          (완전히 RESTful하게 재작성)
├── dto/                              (신규 패키지)
│   ├── CreateUserRequest.java       (신규)
│   ├── UpdateUserRequest.java       (신규)
│   ├── PatchUserRequest.java        (신규)
│   └── UserResponse.java            (신규)
├── service/
│   ├── CalculatorService.java
│   └── UserService.java             (patchUser 메서드 추가)
├── repository/
│   └── UserRepository.java
├── model/
│   └── User.java
└── examples/
    └── DependencyInjectionExamples.java
```

#### 핵심 깨달음

1. **RESTful API는 직관적이다**
   - URI만 보고도 어떤 리소스인지 알 수 있음
   - HTTP 메서드로 의도가 명확함

2. **DTO는 필수다**
   - Entity 직접 노출은 보안 위험
   - API와 도메인 모델 분리

3. **적절한 HTTP 상태 코드 사용**
   - 201 Created는 생성 시만
   - 204 No Content는 삭제 성공 시
   - 404 Not Found는 리소스 없을 때

4. **PUT과 PATCH는 다르다**
   - PUT: 전체 교체 (모든 필드 필수)
   - PATCH: 부분 수정 (변경 필드만)

5. **@RequestBody는 강력하다**
   - JSON을 자동으로 Java 객체로 변환
   - 유효성 검증도 DTO에서 가능

#### 다음 학습 주제

레벨 4를 완료했습니다! 다음 단계 추천:

1. **데이터베이스 연동 (JPA)** - 메모리 → 실제 DB
2. **유효성 검증 (@Valid)** - 더 체계적인 검증
3. **예외 처리** - @ControllerAdvice로 전역 처리

---

## Claude Code 사용 팁

### 새 세션 시작 시

1. **이전 학습 내용 확인**
   ```
   LEARNING_LOG.md를 읽어줘
   ```

2. **다음 단계 진행**
   ```
   NEXT_STEPS.md를 읽어주고, Service 레이어를 추가해줘
   ```

3. **코드 설명 요청**
   ```
   HelloController.java의 각 메서드를 자세히 설명해줘
   ```

### 학습 진행 시 기록 방법

새로운 내용을 학습한 후에는:

```
LEARNING_LOG.md 파일을 업데이트해서 오늘 배운 내용을 추가해줘
```

---

## 학습 통계

- **총 학습 세션**: 2회
- **생성한 파일 수**: 12개
- **구현한 API 엔드포인트**: 15개
- **학습한 어노테이션**: 8개 (@SpringBootApplication, @RestController, @GetMapping, @RequestParam, @PathVariable, @Service, @Repository, @Component)
- **학습한 디자인 패턴**: 계층 구조 (Layered Architecture), 의존성 ��입 (Dependency Injection)
- **완료한 레벨**: 레벨 1 (기본), 레벨 2 (Service 레이어) ✅
- **다음 학습 주제**: 데이터베이스 연동 (JPA) 또는 POST/PUT/DELETE 메서드
---

### 세션 4: 명세 우선 개발 (Specification-First Development) (2025-11-06)

#### 학습 목표
- OpenAPI 명세 우선 개발 방식 이해
- Code-First vs Spec-First 차이 학습
- OpenAPI Generator를 이용한 자동 코드 생성
- 모듈화된 OpenAPI 구조 구축

#### 완료한 작업

1. **명세 우선 개발 전환**
   - OpenAPI Generator Gradle 플러그인 추가
   - API 명세를 코드보다 먼저 작성하는 방식으로 전환

2. **모듈화된 OpenAPI 구조 구축**
   - 메인: [api-spec.yaml](src/main/resources/openapi/api-spec.yaml)
   - 경로별: `src/main/resources/openapi/paths/`
   - 스키마: `src/main/resources/openapi/schemas/`
   - $ref를 사용한 파일 분리

3. **문서 작성**
   - [SPEC_FIRST_DEVELOPMENT.md](SPEC_FIRST_DEVELOPMENT.md)
   - [MODULAR_SPEC.md](MODULAR_SPEC.md)
   - [LAYER_SEPARATION.md](LAYER_SEPARATION.md)
   - [WHY_NOT_API_MODEL_IN_SERVICE.md](WHY_NOT_API_MODEL_IN_SERVICE.md)

#### 학습한 핵심 개념

**명세 우선 개발:**
```
OpenAPI 명세 작성 → 코드 생성 → 구현
```
- 명세가 항상 정확
- 프론트엔드와 동시 개발 가능
- 계약 기반 개발

**계층별 모델 분리:**
- API 계층: OpenAPI 생성 모델 (CreateUserRequestDto)
- Domain 계층: 직접 작성 모델 (User Entity)
- Controller가 변환 책임

---

### 세션 5: 데이터베이스 명세 관리 (dbmate + PostgreSQL) (2025-11-06)

#### 학습 목표
- DB 스키마도 명세로 관리 (dbmate)
- PostgreSQL 연동 (Docker)
- JPA Entity와 dbmate 스키마 매핑
- 명세 우선 개발을 DB까지 확장

#### 완료한 작업

1. **PostgreSQL 환경 구축**
   - [docker-compose.yml](docker-compose.yml) 작성
   - PostgreSQL 16 컨테이너 설정

2. **dbmate 설정**
   - 마이그레이션 파일들:
     - [20250101000001_create_users_table.sql](database/db/migrations/20250101000001_create_users_table.sql)
     - [20250101000002_add_email_index.sql](database/db/migrations/20250101000002_add_email_index.sql)
     - [20250101000003_add_updated_at_trigger.sql](database/db/migrations/20250101000003_add_updated_at_trigger.sql)

3. **Entity 변환**
   - [User.java](src/main/java/com/example/springbasic/model/User.java)
   - Java record → JPA Entity class로 변환
   - `@PrePersist`, `@PreUpdate` 추가

4. **Repository 변환**
   - [UserRepository.java](src/main/java/com/example/springbasic/repository/UserRepository.java)
   - HashMap → JpaRepository 인터페이스
   - Spring Data JPA 쿼리 메서드 사용

5. **문서 작성**
   - [DB_SPEC_MANAGEMENT.md](DB_SPEC_MANAGEMENT.md)
   - [VERIFY_DB_MAPPING.md](VERIFY_DB_MAPPING.md)

#### 학습한 핵심 개념

**dbmate:**
- DB 스키마 버전 관리
- SQL 마이그레이션으로 변경 이력 추적
- 롤백 가능

**ddl-auto: validate:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Entity와 DB 스키마 자동 검증
```
- Entity와 DB 불일치 시 시작 실패
- 안전장치 역할

**JPA Entity:**
- record는 불변이라 JPA 부적합
- Entity는 가변 클래스
- getter/setter 필요

**명세 우선 개발 (API + DB):**
```
OpenAPI 명세 (API)  ↔  dbmate 명세 (DB)
      ↓                      ↓
  Controller  ↔  Service  ↔  Entity
```

#### 검증 방법

1. **자동 검증**: `./gradlew bootRun` (ddl-auto: validate)
2. **수동 확인**: `\d users` (PostgreSQL)
3. **데이터 테스트**: POST → DB 확인 → GET

#### 핵심 깨달음

1. **DB도 명세로 관리**: dbmate 마이그레이션이 Git에 기록
2. **ddl-auto: validate는 필수**: 매핑 불일치 자동 감지
3. **한번 실행된 마이그레이션은 수정 금지**: 새 마이그레이션 추가만
4. **API와 DB 모두 명세 우선**: 완전한 Spec-First 아키텍처

---

---

### 세션 6: 전역 예외 처리 (@RestControllerAdvice) (2025-11-19)

#### 학습 목표
- 커스텀 예외 클래스 작성
- @RestControllerAdvice로 전역 예외 처리
- HTTP 상태 코드별 예외 처리
- 일관된 에러 응답 형식 구축

#### 완료한 작업

1. **커스텀 예외 클래스 작성**
   - [UserNotFoundException.java](src/main/java/com/example/springbasic/exception/UserNotFoundException.java) - 404 에러용
   - [DuplicateEmailException.java](src/main/java/com/example/springbasic/exception/DuplicateEmailException.java) - 중복 이메일용
   - [InvalidUserDataException.java](src/main/java/com/example/springbasic/exception/InvalidUserDataException.java) - 잘못된 데이터용

2. **에러 응답 DTO 작성**
   - [ErrorResponse.java](src/main/java/com/example/springbasic/dto/ErrorResponse.java)
   - 일관된 에러 응답 형식 (status, error, message, path)

3. **전역 예외 처리기 구현**
   - [GlobalExceptionHandler.java](src/main/java/com/example/springbasic/exception/GlobalExceptionHandler.java)
   - @RestControllerAdvice 사용
   - 예외 타입별 @ExceptionHandler 메서드 작성

4. **Service 계층 개선**
   - IllegalArgumentException → 커스텀 예외로 교체
   - 명확한 예외 타입 사용

#### 학습한 핵심 개념

**@RestControllerAdvice:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(...) {
        // 404 Not Found 반환
    }
}
```
- 모든 @RestController의 예외를 한 곳에서 처리
- Controller에서 try-catch 불필요
- 일관된 에러 응답 형식 보장

**커스텀 예외:**
```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("사용자를 찾을 수 없습니다. ID: " + id);
    }
}
```

**에러 응답 형식:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "사용자를 찾을 수 없습니다. ID: 1",
  "path": "/api/users/1"
}
```

#### 핵심 깨달음

1. **Controller가 깔끔해진다**: try-catch 블록 제거
2. **일관된 에러 응답**: 모든 API에서 동일한 형식
3. **예외의 의미가 명확**: UserNotFoundException vs IllegalArgumentException
4. **유지보수 용이**: 에러 처리 로직이 한 곳에 집중

---

### 세션 7: Bean Validation (@Valid) (2025-11-19)

#### 학습 목표
- Bean Validation 사용법 이해
- @Valid로 DTO 자동 검증
- 커스텀 검증 메시지 작성
- 검증 실패 시 에러 응답

#### 완료한 작업

1. **DTO에 검증 어노테이션 추가**
   - OpenAPI 명세에 validation 규칙 정의
   - 자동 생성된 DTO에 검증 어노테이션 포함
   - @NotBlank, @Email, @Min, @Max 등 사용

2. **Controller에 @Valid 적용**
   - [UsersApiController.java](src/main/java/com/example/springbasic/controller/UsersApiController.java)에서 자동 생성된 인터페이스 구현
   - @Valid 어노테이션으로 요청 검증

3. **검증 실패 처리**
   - [GlobalExceptionHandler.java](src/main/java/com/example/springbasic/exception/GlobalExceptionHandler.java)
   - MethodArgumentNotValidException 처리
   - 검증 오류 메시지 포맷팅

#### 학습한 핵심 개념

**Bean Validation 어노테이션:**
```java
public class CreateUserRequestDto {
    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @NotBlank(message = "이메일은 필수입니다")
    private String email;

    @Min(value = 1, message = "나이는 1 이상이어야 합니다")
    @Max(value = 150, message = "나이는 150 이하여야 합니다")
    private Integer age;
}
```

**@Valid 사용:**
```java
@PostMapping
public ResponseEntity<UserResponse> createUser(
    @Valid @RequestBody CreateUserRequest request
) {
    // 검증 통과 시에만 실행됨
}
```

**검증 실패 처리:**
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationException(...) {
    String errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    // 400 Bad Request 반환
}
```

#### 핵심 깨달음

1. **검증 로직이 선언적**: if문 대신 어노테이션
2. **재사용 가능**: 같은 DTO를 여러 곳에서 사용 가능
3. **명세 기반 검증**: OpenAPI 명세에서 자동 생성
4. **에러 메시지 통일**: 일관된 검증 에러 형식

---

### 세션 8: 트랜잭션 관리 및 통합 테스트 (2025-11-19)

#### 학습 목표
- @Transactional 어노테이션 이해
- 트랜잭션 경계와 롤백
- readOnly 최적화
- Spring Boot 통합 테스트 작성

#### 완료한 작업

1. **Service에 트랜잭션 적용**
   - [UserService.java](src/main/java/com/example/springbasic/service/UserService.java)
   - 쓰기 작업: @Transactional
   - 읽기 작업: @Transactional(readOnly = true)

2. **통합 테스트 작성**
   - [UserServiceIntegrationTest.java](src/test/java/com/example/springbasic/service/UserServiceIntegrationTest.java)
   - @SpringBootTest로 실제 DB 테스트
   - 트랜잭션 롤백 검증

3. **페이징 기능 추가**
   - getAllUsers(Pageable) 메서드 추가
   - PagedUserResponse DTO 생성
   - Spring Data JPA Pageable 활용

4. **테스트용 설정 추가**
   - [application-test.yml](src/test/resources/application-test.yml)
   - 테스트 전용 DB 설정

#### 학습한 핵심 개념

**@Transactional의 역할:**
```java
@Transactional
public User createUser(String name, String email, int age) {
    // 1. 중복 이메일 체크 (조회)
    if (userRepository.findByEmail(email).isPresent()) {
        throw new DuplicateEmailException(email);
    }
    // 2. 저장
    return userRepository.save(newUser);
    // 예외 발생 시 자동 롤백!
}
```
- 메서드 전체가 하나의 트랜잭션
- 중간에 예외 발생 시 모든 변경사항 롤백
- ACID 속성 보장

**readOnly 최적화:**
```java
@Transactional(readOnly = true)
public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}
```
- 읽기 전용 명시 → DB 최적화
- flush 생략 가능
- 성능 향상

**Spring Data JPA 페이징:**
```java
// Service
public Page<User> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
}

// Controller
Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
Page<User> userPage = userService.getAllUsers(pageable);
```

**통합 테스트:**
```java
@SpringBootTest  // 전체 Spring 컨텍스트 로드
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Test
    void createUser_Success() {
        User user = userService.createUser("홍길동", "hong@example.com", 25);
        assertThat(user.getId()).isNotNull();  // 실제 DB에서 ID 생성됨
    }
}
```

#### 핵심 깨달음

1. **트랜잭션은 Service 계층**: Controller X, Repository X
2. **readOnly는 성능 최적화**: 읽기 작업에 필수
3. **통합 테스트로 전체 검증**: Mock 대신 실제 DB 사용
4. **페이징으로 대용량 데이터 처리**: 성능 문제 방지

---

### 세션 9: 단위 테스트 (Mockito) (2025-11-19)

#### 학습 목표
- Mockito를 사용한 단위 테스트 작성
- Mock 객체의 개념과 사용법
- Service와 Controller 단위 테스트
- 통합 테스트와의 차이 이해

#### 완료한 작업

1. **UserService 단위 테스트**
   - [UserServiceTest.java](src/test/java/com/example/springbasic/service/UserServiceTest.java)
   - @Mock으로 UserRepository 모킹
   - @InjectMocks로 UserService 테스트
   - 9개 테스트 메서드 (CRUD + 예외 케이스)

2. **UsersApiController 단위 테스트**
   - [UsersApiControllerTest.java](src/test/java/com/example/springbasic/controller/UsersApiControllerTest.java)
   - @WebMvcTest로 Controller만 테스트
   - MockMvc로 HTTP 요청 시뮬레이션
   - @MockBean으로 UserService 모킹
   - 13개 테스트 메서드 (모든 HTTP 엔드포인트)

3. **User 엔티티 수정**
   - setId() 메서드 추가 (테스트 전용)

#### 학습한 핵심 개념

**Mockito 기본:**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;  // 가짜 객체

    @InjectMocks
    private UserService userService;  // Mock 주입받음

    @Test
    void createUser_Success() {
        when(userRepository.save(any())).thenReturn(savedUser);
        User result = userService.createUser("홍길동", "hong@example.com", 25);
        verify(userRepository).save(any());
    }
}
```

**MockMvc 사용:**
```java
@WebMvcTest(UsersApiController.class)
class UsersApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void createUser_Success() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{...}"))
            .andExpect(status().isCreated());
    }
}
```

**단위 테스트 vs 통합 테스트:**
- 단위: Mock 사용, 빠름, 특정 계층만
- 통합: 실제 DB, 느림, 전체 시스템

#### 핵심 깨달음

1. **빠른 피드백**: 단위 테스트는 0.1초 이내
2. **계층별 테스트**: Service와 Controller 분리 테스트
3. **Mock의 힘**: 의존성 없이 독립적 테스트
4. **검증 패턴**: when().thenReturn(), verify()

---

### 세션 10: Swagger UI 자동 문서화 (2025-11-19)

#### 학습 목표
- Springdoc OpenAPI로 Swagger UI 설정
- 기존 OpenAPI 명세와 연동
- 브라우저에서 API 테스트

#### 완료한 작업

1. **Swagger UI 설정**
   - build.gradle에 springdoc-openapi 의존성 추가
   - [application.yml](src/main/resources/application.yml)에 Swagger 설정
   - [OpenApiConfig.java](src/main/java/com/example/springbasic/config/OpenApiConfig.java) 업데이트

2. **접속 확인**
   - http://localhost:8080/swagger-ui.html
   - http://localhost:8080/api-docs

#### 학습한 핵심 개념

**Swagger UI 장점:**
- 자동 문서 생성
- 브라우저에서 API 테스트
- 프론트엔드와 스펙 공유

**설정:**
```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /api-docs
```

---

### 세션 12: JPA 연관관계 매핑 및 N+1 문제 (2025-11-19)

#### 학습 목표
- @OneToMany, @ManyToOne 연관관계
- FetchType.LAZY 지연 로딩
- N+1 문제 이해 및 해결
- Fetch Join 사용법

#### 완료한 작업

1. **엔티티 추가**
   - [Post.java](src/main/java/com/example/springbasic/model/Post.java) - 게시글
   - [Comment.java](src/main/java/com/example/springbasic/model/Comment.java) - 댓글
   - [User.java](src/main/java/com/example/springbasic/model/User.java) 수정 (posts 관계 추가)

2. **Repository 추가**
   - [PostRepository.java](src/main/java/com/example/springbasic/repository/PostRepository.java)
   - [CommentRepository.java](src/main/java/com/example/springbasic/repository/CommentRepository.java)
   - Fetch Join 쿼리 메서드

3. **DB 스키마**
   - [20250101000004_create_posts_table.sql](database/db/migrations/20250101000004_create_posts_table.sql)
   - [20250101000005_create_comments_table.sql](database/db/migrations/20250101000005_create_comments_table.sql)
   - [schema.dbml](database/schema.dbml) 업데이트

4. **N+1 테스트**
   - [NPlusOneProblemTest.java](src/test/java/com/example/springbasic/repository/NPlusOneProblemTest.java)

#### 학습한 핵심 개념

**연관관계:**
```
User (1) ────< (N) Post (1) ────< (N) Comment
```

**N+1 문제:**
```java
// ❌ N+1 발생
List<Post> posts = postRepository.findAll();  // 1개 쿼리
for (Post post : posts) {
    post.getAuthor().getName();  // N개 쿼리!
}

// ✅ Fetch Join으로 해결
@Query("SELECT p FROM Post p JOIN FETCH p.author")
List<Post> findAllWithAuthor();  // 1개 쿼리로 해결!
```

**FetchType.LAZY:**
- 기본값으로 사용
- 필요할 때만 조회
- N+1 문제 주의

#### 핵심 깨달음

1. **N+1은 성능 킬러**: 100개 데이터 = 101개 쿼리
2. **Fetch Join이 해결책**: 1개 JOIN 쿼리로 해결
3. **LAZY는 필수**: EAGER는 사용 금지
4. **SQL 로그 확인**: show-sql로 쿼리 개수 확인

---

### 세션 13: QueryDSL - 타입 안전한 쿼리 (2025-11-19)

#### 학습 목표
- QueryDSL 설정 및 Q-Type 생성
- JPQL 문자열 → Java 코드
- 타입 안전성과 컴파일 타임 검증
- 동적 쿼리 작성 (BooleanBuilder)

#### 완료한 작업

1. **QueryDSL 설정**
   - build.gradle에 QueryDSL 의존성 추가
   - Annotation Processor 설정
   - Q-Type 클래스 자동 생성 (QUser, QPost, QComment)

2. **Config 작성**
   - [QueryDslConfig.java](src/main/java/com/example/springbasic/config/QueryDslConfig.java)
   - JPAQueryFactory Bean 등록

3. **Custom Repository**
   - [CommentRepositoryCustom.java](src/main/java/com/example/springbasic/repository/CommentRepositoryCustom.java) - 인터페이스
   - [CommentRepositoryImpl.java](src/main/java/com/example/springbasic/repository/CommentRepositoryImpl.java) - 구현체
   - [CommentRepository.java](src/main/java/com/example/springbasic/repository/CommentRepository.java) 수정 (Custom 상속)

4. **비교 테스트**
   - [QueryDslVsJpqlTest.java](src/test/java/com/example/springbasic/repository/QueryDslVsJpqlTest.java)

#### 학습한 핵심 개념

**JPQL vs QueryDSL:**
```java
// JPQL: 문자열, 런타임 에러
@Query("SELECT c FROM Comment c WHERE c.post.id = :postId")

// QueryDSL: Java 코드, 컴파일 타임 에러
queryFactory
    .selectFrom(comment)
    .where(comment.post.id.eq(postId))
    .fetch();
```

**동적 쿼리:**
```java
BooleanBuilder builder = new BooleanBuilder();
if (content != null) {
    builder.and(comment.content.contains(content));
}
if (postId != null) {
    builder.and(comment.post.id.eq(postId));
}
```

**QueryDSL 장점:**
- ✅ 타입 안전성 (컴파일 타임 검증)
- ✅ IDE 자동완성
- ✅ 리팩토링 자동 추적
- ✅ 동적 쿼리 작성 쉬움

#### 핵심 깨달음

1. **문자열 → 코드**: JPQL의 취약점 해결
2. **컴파일 타임 검증**: 오타를 즉시 발견
3. **동적 쿼리의 힘**: 조건 조합 자유자재
4. **JPA와 공존**: Spring Data JPA와 함께 사용

---

---

### 세션 14: 프로젝트 문서화 및 dbmate 통합 개선 (2025-11-20)

#### 학습 목표
- Liquibase에서 dbmate로의 마이그레이션 도구 변경 문서화
- CLAUDE.md 작성으로 향후 개발 가이드 제공
- 프로젝트 문서 일관성 유지

#### 완료한 작업

1. **문서 일관성 개선**
   - 모든 문서에서 "Liquibase" 참조를 "dbmate"로 통일
   - 수정 파일:
     - [README.md](../README.md)
     - [SESSION_CONTEXT.md](SESSION_CONTEXT.md)
     - [DB_SPEC_MANAGEMENT.md](DB_SPEC_MANAGEMENT.md)
     - [database/README.md](../database/README.md)

2. **CLAUDE.md 작성**
   - [CLAUDE.md](../CLAUDE.md) 신규 생성
   - Claude Code를 위한 프로젝트 가이드 작성
   - 핵심 명령어 및 워크플로우 정리
   - 아키텍처 및 디자인 패턴 문서화
   - 일반적인 함정 및 해결 방법 추가

3. **커밋 메시지 작성**
   - 변경사항을 명확하게 설명하는 커밋 메시지
   - 기술 스택 및 다음 단계 명시

#### 학습한 핵심 개념

**프로젝트 문서화의 중요성:**
- CLAUDE.md는 향후 Claude Code 세션을 위한 "컨텍스트 파일"
- 신규 개발자(또는 새 AI 세션)가 빠르게 온보딩 가능
- 프로젝트의 철학과 원칙을 명시

**문서 일관성:**
- 도구 변경 시 모든 문서를 업데이트해야 함
- 문서 불일치는 혼란과 실수로 이어짐
- Grep을 통한 전체 검색으로 모든 참조 찾기

**명세 우선 개발 문서화:**
```
OpenAPI Spec → Generated Code → Implementation
                                      ↓
                                API ↔ Domain
                                      ↓
                                   Service
                                      ↓
dbmate Spec → DB Schema → PostgreSQL
```

#### CLAUDE.md 주요 내용

1. **필수 명령어**
   - 데이터베이스 관리: `docker-compose up -d`, `dbmate up`
   - API 개발: `./gradlew generateApi`, `./gradlew bootRun`
   - 개발 워크플로우 (API 변경 / DB 변경)

2. **고수준 아키텍처**
   - 명세 우선 철학 설명
   - 계층 분리 (API vs Domain)
   - 데이터베이스 독립성 (dbmate 독립 실행)
   - 생성된 코드 위치

3. **핵심 디자인 패턴**
   - OpenAPI 모듈화 구조
   - 생성자 의존성 주입
   - 전역 예외 처리
   - 트랜잭션 관리
   - JPA 베스트 프랙티스

4. **파일 조직**
   - Controllers: API 인터페이스 구현, DTO ↔ Entity 변환
   - Services: 비즈니스 로직, Domain 모델 사용
   - Repositories: JpaRepository, QueryDSL 커스텀
   - DTOs: 자동 생성, 수동 수정 금지

5. **테스트 전략**
   - 단위 테스트: Mock 사용, 빠름
   - 통합 테스트: 실제 DB, 전체 스택

6. **일반적인 함정**
   - 생성된 코드 수정 금지
   - Service에서 API DTO 사용 금지
   - ddl-auto: create 사용 금지
   - EAGER 페칭 사용 금지

#### 문서 구조

```
docs/
├── LEARNING_LOG.md           # 학습 이력 (14개 세션)
├── SESSION_CONTEXT.md        # 빠른 참조용 컨텍스트
├── DB_SPEC_MANAGEMENT.md     # dbmate 사용법
├── WHY_NOT_API_MODEL_IN_SERVICE.md  # 계층 분리 이유
├── SPEC_FIRST_DEVELOPMENT.md # 명세 우선 개발
└── ...

CLAUDE.md                     # Claude Code 가이드 (신규)
README.md                     # 프로젝트 개요
```

#### 핵심 깨달음

1. **문서는 코드만큼 중요**
   - 좋은 문서는 온보딩 시간을 크게 단축
   - 프로젝트 원칙을 명시하면 실수 방지

2. **AI를 위한 문서 (CLAUDE.md)**
   - AI는 컨텍스트가 중요
   - 명확한 가이드로 일관된 코드 생성
   - "왜"를 설명하면 더 나은 결정

3. **도구 변경은 전체 문서 업데이트 필요**
   - Liquibase → dbmate 변경
   - 모든 참조를 일관되게 수정

4. **명세 우선의 힘**
   - API: OpenAPI 명세
   - DB: dbmate 마이그레이션
   - 코드: 자동 생성 또는 명세 기반
   - Git에 모든 명세가 기록됨

---

## 업데이트된 학습 통계

- **총 학습 세션**: 14회
- **생성한 파일 수**: 46개 이상 (CLAUDE.md 추가)
- **구현한 API 엔드포인트**: 9개 (RESTful + 페이징)
- **구현한 엔티티**: 3개 (User, Post, Comment)
- **학습한 어노테이션**:
  - Spring: @SpringBootApplication, @RestController, @Service, @Repository, @GetMapping, @PostMapping, @PutMapping, @PatchMapping, @DeleteMapping, @RequestParam, @PathVariable, @RequestBody, @RestControllerAdvice, @ExceptionHandler, @Valid
  - JPA: @Entity, @Table, @Id, @GeneratedValue, @Column, @PrePersist, @PreUpdate, @OneToMany, @ManyToOne, @JoinColumn
  - Transaction: @Transactional
  - Validation: @NotNull, @NotBlank, @Email, @Min, @Max, @Size
  - Test: @SpringBootTest, @Test, @DisplayName, @AfterEach, @ExtendWith, @Mock, @InjectMocks, @WebMvcTest, @MockBean
- **학습한 디자인 패턴**:
  - Layered Architecture (계층 구조)
  - Dependency Injection (의존성 주입)
  - DTO Pattern (데이터 전송 객체)
  - Repository Pattern (데이터 접근 추상화)
  - Specification-First Development (명세 우선 개발)
  - Global Exception Handling (전역 예외 처리)
  - Custom Repository Pattern (QueryDSL)
- **학습한 도구**:
  - OpenAPI Generator (API 코드 생성)
  - dbmate (DB 마이그레이션)
  - PostgreSQL (관계형 데이터베이스)
  - Docker Compose (컨테이너 오케스트레이션)
  - Spring Data JPA (ORM)
  - QueryDSL (타입 안전 쿼리)
  - Mockito (Mock 프레임워크)
  - JUnit 5 (테스트 프레임워크)
  - AssertJ (테스트 assertion 라이브러리)
  - Springdoc OpenAPI (Swagger UI)
- **완료한 레벨**:
  - ✅ 레벨 1-5: Spring Boot 기초 + 명세 우선 개발
  - ✅ 레벨 6: 전역 예외 처리 (@RestControllerAdvice)
  - ✅ 레벨 7: Bean Validation (@Valid)
  - ✅ 레벨 8: 트랜잭션 & 통합 테스트
  - ✅ 레벨 9: 단위 테스트 (Mockito)
  - ✅ 레벨 10: Swagger UI
  - ✅ 레벨 12: JPA 연관관계 + N+1 문제
  - ✅ 레벨 13: QueryDSL (타입 안전 쿼리)
  - ✅ 레벨 14: 프로젝트 문서화 (CLAUDE.md)
- **다음 학습 주제**:
  - Spring Security (인증/인가)
  - 성능 최적화 (캐싱, 인덱스)
  - 비동기 처리 (@Async, Event)
  - 배포 (Docker, CI/CD)
