# 왜 Service에서 API 모델을 사용하면 안 되는가?

## 🎯 핵심 개념: 관심사의 분리 (Separation of Concerns)

```
외부 세계 (API)와 내부 세계 (비즈니스 로직)는 서로 독립적이어야 한다
```

---

## 📊 구체적인 문제 상황들

### 문제 1: API 변경에 취약해짐 🔴

#### 나쁜 예 (Service가 API 모델 사용)

```java
// ❌ Service가 API 모델에 의존
@Service
public class UserService {
    public UserResponse createUser(CreateUserRequest request) {
        // 비즈니스 로직
        User user = new User(
            null,
            request.getName(),
            request.getEmail(),
            request.getAge()
        );

        User saved = userRepository.save(user);

        // API 모델 생성
        return new UserResponse()
            .id(saved.id())
            .name(saved.name())
            .email(saved.email())
            .age(saved.age());
    }
}
```

**시나리오: API 명세 변경**

OpenAPI 명세 수정:
```yaml
# CreateUserRequest에 phoneNumber 추가
properties:
  name:
    type: string
  email:
    type: string
  age:
    type: integer
  phoneNumber:  # 새 필드!
    type: string
```

**영향 범위:**
```
./gradlew generateApi 실행
    ↓
CreateUserRequest가 자동으로 변경됨
    ↓
❌ UserService 컴파일 에러!
    → createUser(CreateUserRequest request)의 시그니처는 그대로인데
    → CreateUserRequest 내부 구조가 변경되어
    → 비즈니스 로직이 깨짐
```

#### 좋은 예 (Service가 도메인 모델 사용)

```java
// ✅ Service는 도메인 모델만 사용
@Service
public class UserService {
    public User createUser(String name, String email, int age) {
        // 순수 비즈니스 로직
        User user = User.createNew(name, email, age);
        return userRepository.save(user);
    }
}
```

**같은 시나리오에서:**
```
./gradlew generateApi 실행
    ↓
CreateUserRequest 변경됨
    ↓
✅ UserService는 영향 없음!
    → Controller만 수정하면 됨
    → 비즈니스 로직은 안정적
```

---

### 문제 2: 비즈니스 로직 재사용 불가 🔴

#### 시나리오: 여러 API에서 같은 비즈니스 로직 사용

```
요구사항:
1. REST API: JSON으로 사용자 생성
2. GraphQL API: GraphQL로 사용자 생성
3. Admin API: 관리자용 내부 API
4. Batch Job: CSV 파일로 대량 사용자 생성
```

#### 나쁜 예

```java
// ❌ REST API 전용으로 고정됨
@Service
public class UserService {
    public UserResponse createUser(CreateUserRequest request) {
        // CreateUserRequest = REST API 명세에서 생성된 모델
        // 다른 API에서는 못 씀!
    }
}
```

**문제:**
```java
// GraphQL에서 사용하려면?
@GraphQLMutation
public GraphQLUserResponse createUser(GraphQLCreateUserInput input) {
    // ❌ UserService를 못 씀!
    // CreateUserRequest와 GraphQLCreateUserInput은 다른 타입

    // 어쩔 수 없이 중복 코드 작성
    User user = new User(...);  // 똑같은 로직 반복
    userRepository.save(user);
}

// Batch Job에서 사용하려면?
public void importUsersFromCsv(List<CsvUser> csvUsers) {
    // ❌ UserService를 못 씀!

    // 또 중복 코드
    for (CsvUser csv : csvUsers) {
        User user = new User(...);  // 또 반복
        userRepository.save(user);
    }
}
```

#### 좋은 예

```java
// ✅ 어디서든 재사용 가능
@Service
public class UserService {
    public User createUser(String name, String email, int age) {
        // 순수 비즈니스 로직
        // 파라미터는 기본 타입 또는 도메인 모델
    }
}
```

**해결:**
```java
// REST API
@RestController
public class UsersApiController {
    public ResponseEntity<UserResponse> createUser(CreateUserRequest request) {
        User user = userService.createUser(
            request.getName(),
            request.getEmail(),
            request.getAge()
        );  // ✅ 재사용!
        return ResponseEntity.ok(mapToResponse(user));
    }
}

// GraphQL API
@GraphQLMutation
public GraphQLUserResponse createUser(GraphQLCreateUserInput input) {
    User user = userService.createUser(
        input.getName(),
        input.getEmail(),
        input.getAge()
    );  // ✅ 같은 Service 재사용!
    return mapToGraphQLResponse(user);
}

// Batch Job
public void importUsersFromCsv(List<CsvUser> csvUsers) {
    for (CsvUser csv : csvUsers) {
        userService.createUser(
            csv.getName(),
            csv.getEmail(),
            csv.getAge()
        );  // ✅ 같은 Service 재사용!
    }
}
```

---

### 문제 3: 테스트 복잡도 증가 🔴

#### 나쁜 예

```java
// ❌ Service 테스트에 API 모델 필요
@Test
void createUser_success() {
    // API 모델 생성 (복잡함)
    CreateUserRequest request = new CreateUserRequest()
        .name("홍길동")
        .email("hong@example.com")
        .age(25);

    // Service 호출
    UserResponse response = userService.createUser(request);

    // 검증도 API 모델로
    assertThat(response.getName()).isEqualTo("홍길동");
}
```

**문제:**
1. API 모델의 생성 방식을 알아야 함
2. API 명세 변경 시 테스트 깨짐
3. 비즈니스 로직 테스트인데 API 구조에 종속

#### 좋은 예

```java
// ✅ 순수하게 비즈니스 로직만 테스트
@Test
void createUser_success() {
    // 간단한 파라미터
    User user = userService.createUser("홍길동", "hong@example.com", 25);

    // 도메인 모델로 검증
    assertThat(user.name()).isEqualTo("홍길동");
    assertThat(user.email()).isEqualTo("hong@example.com");
}

@Test
void createUser_duplicateEmail_throwsException() {
    // Given
    userService.createUser("김철수", "test@example.com", 30);

    // When & Then
    assertThatThrownBy(() ->
        userService.createUser("이영희", "test@example.com", 25)
    ).isInstanceOf(IllegalArgumentException.class)
     .hasMessageContaining("이미 존재하는 이메일");
}
```

---

### 문제 4: 순환 의존성 위험 🔴

#### 나쁜 예 (실제 발생 가능한 문제)

```java
// API 모델 (자동 생성)
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private int age;

    // Swagger UI를 위한 예제 데이터
    @Schema(example = "...")
    public static UserResponse example() {
        // 예제 데이터 생성을 위해 Service 호출?
        return UserService.getSampleUser();  // ❌ 순환 참조!
    }
}

// Service
@Service
public class UserService {
    public UserResponse createUser(CreateUserRequest request) {
        // UserResponse 사용
    }
}
```

**결과:**
```
API 모델 → Service 의존
Service → API 모델 의존
    ↓
순환 의존성 발생!
```

---

### 문제 5: API 버저닝 문제 🔴

#### 시나리오: API v1과 v2를 동시 운영

```
요구사항:
- v1: /api/v1/users (기존 API)
- v2: /api/v2/users (개선된 API, 다른 응답 형식)
```

#### 나쁜 예

```java
// ❌ Service가 특정 버전에 종속
@Service
public class UserService {
    public V1UserResponse createUser(V1CreateUserRequest request) {
        // v1 전용
    }
}

// v2를 추가하려면?
@Service
public class UserServiceV2 {
    public V2UserResponse createUser(V2CreateUserRequest request) {
        // ❌ 비즈니스 로직 중복!
        // 똑같은 사용자 생성 로직을 또 작성해야 함
    }
}
```

#### 좋은 예

```java
// ✅ Service는 버전과 무관
@Service
public class UserService {
    public User createUser(String name, String email, int age) {
        // 순수 비즈니스 로직 (버전 무관)
    }
}

// v1 Controller
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller {
    public V1UserResponse createUser(V1CreateUserRequest request) {
        User user = userService.createUser(...);  // ✅ 재사용
        return mapToV1Response(user);
    }
}

// v2 Controller
@RestController
@RequestMapping("/api/v2/users")
public class UserV2Controller {
    public V2UserResponse createUser(V2CreateUserRequest request) {
        User user = userService.createUser(...);  // ✅ 같은 Service 재사용
        return mapToV2Response(user);
    }
}
```

---

### 문제 6: 불필요한 필드 노출 🔴

#### 나쁜 예

```yaml
# API 명세: 응답에 민감한 정보 포함하지 않음
components:
  schemas:
    UserResponse:
      properties:
        id:
          type: integer
        name:
          type: string
        email:
          type: string
        # password는 응답에 없음!
```

```java
// ❌ Service가 API 모델 반환
@Service
public class UserService {
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();

        // API 모델 생성
        return new UserResponse()
            .id(user.id())
            .name(user.name())
            .email(user.email());
            // password는 자동으로 제외됨
    }
}

// 다른 Service에서 사용하려면?
@Service
public class OrderService {
    public void createOrder(Long userId) {
        UserResponse user = userService.getUser(userId);

        // ❌ 문제: 비즈니스 로직에 password가 필요한데 없음!
        // ❌ API 응답 모델이라 password 필드가 아예 없음

        if (user.getPassword().equals(...)) {  // 컴파일 에러!
            // password 검증 불가
        }
    }
}
```

#### 좋은 예

```java
// ✅ Service는 도메인 모델 반환
@Service
public class UserService {
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}

// Controller에서 필터링
@RestController
public class UserController {
    public ResponseEntity<UserResponse> getUser(Long id) {
        User user = userService.getUser(id);  // 전체 정보

        // API 응답으로 변환 (password 제외)
        UserResponse response = new UserResponse()
            .id(user.id())
            .name(user.name())
            .email(user.email());
            // password는 의도적으로 제외

        return ResponseEntity.ok(response);
    }
}

// 다른 Service에서는 전체 정보 사용 가능
@Service
public class OrderService {
    public void createOrder(Long userId) {
        User user = userService.getUser(userId);  // ✅ 전체 정보

        if (user.password().equals(...)) {  // ✅ 가능!
            // 비즈니스 로직 처리
        }
    }
}
```

---

## 🎯 올바른 설계: 헥사고날 아키텍처 관점

```
┌─────────────────────────────────────────────────────────────┐
│                    외부 세계 (Ports)                          │
│                                                               │
│  REST API     GraphQL     gRPC      Message Queue            │
│  (HTTP)       (HTTP)      (Proto)   (Kafka)                  │
└─────────────────────────────────────────────────────────────┘
                              ↕
                         Adapters
                    (Controller Layer)
                              ↕
                    각자의 모델로 변환
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                   핵심 도메인 (Core)                          │
│                                                               │
│              도메인 모델 + 비즈니스 로직                       │
│                   (Service Layer)                             │
│                                                               │
│  - 외부 기술과 무관                                           │
│  - 순수 Java 객체                                            │
│  - 비즈니스 규칙에만 집중                                      │
└─────────────────────────────────────────────────────────────┘
                              ↕
                    Repository Layer
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                    데이터 저장소                              │
│                                                               │
│  PostgreSQL   MongoDB     Redis     File System              │
└─────────────────────────────────────────────────────────────┘
```

### 핵심 원칙: Dependency Rule

```
외부 계층이 내부 계층을 의존
내부 계층은 외부 계층을 몰라야 함

Controller (외부) → Service (내부) ✅
Service (내부) → Controller (외부) ❌

Controller (외부) → API 모델 ✅
Service (내부) → API 모델 ❌
```

---

## 📊 실제 프로젝트 크기별 영향

### 소규모 프로젝트 (API 10개 미만)

```
잘못된 설계의 영향: 중간
- API 변경 시 여러 곳 수정
- 하지만 프로젝트가 작아서 감당 가능
```

### 중규모 프로젝트 (API 50개)

```
잘못된 설계의 영향: 큼
- API 변경 → 전체 Service 영향
- 테스트 깨짐
- 리팩토링 비용 급증
```

### 대규모 프로젝트 (API 200개 이상)

```
잘못된 설계의 영향: 치명적
- API 버전 관리 불가능
- 마이크로서비스 분리 불가능
- 기술 부채 누적
- 시스템 재작성 필요
```

---

## ✅ 올바른 설계 원칙 정리

### 1. API 모델 (외부)

```java
// build/generated/.../api/model/
// - OpenAPI 명세에서 자동 생성
// - Controller에서만 사용
// - 외부 세계와의 계약
```

**용도:**
- ✅ HTTP 요청/응답 직렬화
- ✅ API 문서 생성
- ✅ 클라이언트 코드 생성
- ❌ 비즈니스 로직 **절대 안됨!**

### 2. 도메인 모델 (내부)

```java
// src/main/java/.../model/
// - 개발자가 직접 작성
// - Service, Repository에서 사용
// - 비즈니스 규칙 표현
```

**용도:**
- ✅ 비즈니스 로직
- ✅ 데이터 검증
- ✅ 도메인 규칙 강제
- ✅ Service 간 데이터 전달

### 3. Controller (변환 계층)

```java
// - 두 세계를 연결
// - API 모델 ↔ 도메인 모델 변환
// - HTTP 관련 처리
```

---

## 💡 실전 체크리스트

Service를 작성할 때 이것을 확인하세요:

### ✅ 좋은 신호

- [ ] 파라미터가 기본 타입 또는 도메인 모델
- [ ] 반환 타입이 도메인 모델
- [ ] `import com.example.api.model.*` 없음
- [ ] 다른 API에서도 재사용 가능
- [ ] 테스트가 간단함

### 🔴 나쁜 신호

- [ ] 파라미터가 API 모델 (`CreateUserRequest` 등)
- [ ] 반환 타입이 API 모델 (`UserResponse` 등)
- [ ] `import com.example.api.model.*` 있음
- [ ] REST API에만 종속
- [ ] 테스트에 API 모델 필요

---

## 🎓 학습 순서

1. **지금:** 계층 분리 이해
2. **다음:** 실제로 API 변경 경험
3. **그 다음:** 두 번째 API (GraphQL, gRPC 등) 추가
4. **나중:** 마이크로서비스 분리

---

## 📚 요약

### 한 문장 요약

> **"Service는 비즈니스의 핵심이다. 외부 API 형식에 종속되어서는 안 된다."**

### 핵심 이유 5가지

1. **변경에 강함**: API 변경 → Service 영향 없음
2. **재사용 가능**: 여러 API에서 같은 Service 사용
3. **테스트 용이**: 순수 비즈니스 로직만 테스트
4. **독립성**: 기술 스택 변경 가능 (REST → gRPC)
5. **확장성**: 버전 관리, 마이크로서비스 전환 용이

---

**현재 프로젝트는 이미 올바르게 설계되어 있습니다!** ✅

계속 이 원칙을 유지하세요! 🎉