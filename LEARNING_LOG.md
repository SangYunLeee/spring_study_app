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