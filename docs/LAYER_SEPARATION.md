# 계층 분리 (Layer Separation) - 명세 기반 개발

명세 기반 개발에서 가장 중요한 개념은 **계층을 명확히 분리**하는 것입니다.

## 🎯 핵심 개념

### API 모델 vs 도메인 모델

```
┌─────────────────────────────────────────────────────────────┐
│                        외부 세계                              │
│                   (클라이언트, 브라우저)                       │
└─────────────────────────────────────────────────────────────┘
                              ↕
                         JSON/HTTP
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                   API 계층 (Controller)                      │
│  - 생성된 API 모델 사용                                       │
│  - UserResponse, CreateUserRequest (자동 생성)              │
│  - OpenAPI 명세에 종속                                       │
└─────────────────────────────────────────────────────────────┘
                              ↕
                      변환 (Mapping)
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                 비즈니스 계층 (Service)                       │
│  - 도메인 모델 사용                                          │
│  - User (직접 작성한 도메인 모델)                            │
│  - 비즈니스 로직에 집중                                       │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                 데이터 계층 (Repository)                      │
│  - 도메인 모델 사용                                          │
│  - DB 접근                                                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 현재 프로젝트 구조

### API 모델 (자동 생성) 🤖

```
build/generated/src/main/java/
└── com/example/springbasic/api/
    ├── UsersApi.java                    # 자동 생성된 인터페이스
    └── model/
        ├── CreateUserRequest.java       # 🤖 자동 생성
        ├── UpdateUserRequest.java       # 🤖 자동 생성
        ├── PatchUserRequest.java        # 🤖 자동 생성
        ├── UserResponse.java            # 🤖 자동 생성
        └── UserStatistics.java          # 🤖 자동 생성
```

**특징:**
- ✅ OpenAPI 명세에서 자동 생성
- ✅ 명세 변경 시 자동 업데이트
- ✅ API 계약 보장
- ❌ 직접 수정 불가 (명세를 수정해야 함)

### 도메인 모델 (수동 작성) ✍️

```
src/main/java/com/example/springbasic/
├── model/
│   └── User.java                        # ✍️ 도메인 모델
├── service/
│   └── UserService.java                 # ✍️ 비즈니스 로직
└── repository/
    └── UserRepository.java              # ✍️ 데이터 접근
```

**특징:**
- ✍️ 개발자가 직접 작성
- ✅ 비즈니스 로직에 최적화
- ✅ 자유롭게 수정 가능
- ✅ DB 구조와 일치

---

## 🔄 변환 (Mapping) - Controller의 역할

### Controller = API와 도메인을 연결하는 다리

[UsersApiController.java](src/main/java/com/example/springbasic/controller/UsersApiController.java):

```java
@RestController
public class UsersApiController implements UsersApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> createUser(
            CreateUserRequest createUserRequest  // 🤖 API 모델 (자동 생성)
    ) {
        // 1️⃣ API 모델 → 도메인으로 변환
        User user = userService.createUser(
            createUserRequest.getName(),
            createUserRequest.getEmail(),
            createUserRequest.getAge()
        );

        // 2️⃣ 도메인 → API 모델로 변환
        UserResponse response = mapToUserResponse(user);  // 🤖 API 모델

        return ResponseEntity.created(...).body(response);
    }

    // 변환 헬퍼 메서드
    private UserResponse mapToUserResponse(User user) {  // ✍️ → 🤖
        return new UserResponse()
            .id(user.id())
            .name(user.name())
            .email(user.email())
            .age(user.age());
    }
}
```

---

## ❓ 왜 분리하는가?

### 문제 상황: 분리하지 않으면?

```java
// ❌ 나쁜 예: Service가 API 모델을 직접 사용
@Service
public class UserService {
    public UserResponse createUser(CreateUserRequest request) {
        // 문제 1: API 명세 변경 시 Service도 변경
        // 문제 2: 비즈니스 로직이 API에 종속됨
        // 문제 3: 테스트 시 API 모델 생성 필요
    }
}
```

**문제점:**
- 🔴 API 명세 변경 → Service 수정 필요
- 🔴 비즈니스 로직이 외부 API에 종속
- 🔴 재사용 어려움 (다른 API에서 못 씀)

### 해결: 계층 분리

```java
// ✅ 좋은 예: Service는 도메인 모델만 사용
@Service
public class UserService {
    public User createUser(String name, String email, int age) {
        // ✅ API와 무관
        // ✅ 순수 비즈니스 로직
        // ✅ 어디서든 재사용 가능

        User newUser = User.createNew(name, email, age);
        return userRepository.save(newUser);
    }
}
```

**장점:**
- ✅ API 명세 변경 → Controller만 수정
- ✅ 비즈니스 로직 독립적
- ✅ 테스트 용이
- ✅ 재사용 가능

---

## 📊 실전 예제: 명세 변경 시나리오

### 시나리오: API에 "phoneNumber" 필드 추가

#### 1️⃣ 명세 수정

```yaml
# schemas/requests/CreateUserRequest.yaml
properties:
  name:
    type: string
  email:
    type: string
  age:
    type: integer
  phoneNumber:  # 새 필드!
    type: string
    example: "010-1234-5678"
```

#### 2️⃣ 코드 재생성

```bash
./gradlew generateApi
```

**결과:** `CreateUserRequest`에 자동으로 `phoneNumber` 추가됨!

#### 3️⃣ 영향 범위

| 계층 | 변경 필요? | 이유 |
|------|----------|------|
| **API 모델** | 🤖 자동 | 명세에서 자동 생성 |
| **Controller** | ✍️ 수동 | 변환 로직 추가 |
| **Service** | ✍️ 수동 | 비즈니스 로직 추가 |
| **Domain** | ✍️ 수동 | User 모델에 필드 추가 |
| **Repository** | ✍️ 수동 | DB 저장 로직 변경 |

#### 4️⃣ 수정 코드

**Controller (변환 추가):**
```java
User user = userService.createUser(
    createUserRequest.getName(),
    createUserRequest.getEmail(),
    createUserRequest.getAge(),
    createUserRequest.getPhoneNumber()  // 새 필드 추가
);
```

**Service (파라미터 추가):**
```java
public User createUser(String name, String email, int age, String phoneNumber) {
    User newUser = User.createNew(name, email, age, phoneNumber);
    return userRepository.save(newUser);
}
```

**Domain (필드 추가):**
```java
public record User(
    Long id,
    String name,
    String email,
    int age,
    String phoneNumber  // 새 필드
) {
    // ...
}
```

---

## 💡 베스트 프랙티스

### ✅ DO - 해야 할 것

1. **Controller에서 변환**
   ```java
   // API 모델 → 도메인
   User user = userService.createUser(
       request.getName(),
       request.getEmail(),
       request.getAge()
   );

   // 도메인 → API 모델
   UserResponse response = mapToUserResponse(user);
   ```

2. **Service는 도메인만**
   ```java
   public User createUser(String name, String email, int age) {
       // 순수 도메인 로직
   }
   ```

3. **명확한 변환 메서드**
   ```java
   private UserResponse mapToUserResponse(User user) {
       return new UserResponse()
           .id(user.id())
           .name(user.name())
           .email(user.email())
           .age(user.age());
   }
   ```

### ❌ DON'T - 하지 말아야 할 것

1. **Service에서 API 모델 사용**
   ```java
   // ❌ 나쁨
   public UserResponse createUser(CreateUserRequest request)
   ```

2. **도메인 모델을 API로 직접 노출**
   ```java
   // ❌ 나쁨
   @PostMapping
   public User createUser(@RequestBody User user)
   ```

3. **변환 로직을 Service에**
   ```java
   // ❌ 나쁨
   public UserResponse toResponse(User user)  // Service에 있으면 안됨!
   ```

---

## 🎯 현재 프로젝트 상태

### ✅ 올바르게 구현됨!

```
API 계층:
  UsersApiController
    ↓ (변환)
비즈니스 계층:
  UserService (도메인 모델만 사용) ✅
    ↓
데이터 계층:
  UserRepository (도메인 모델만 사용) ✅
```

### 확인 방법

1. **UserService 열어보기:**
   ```java
   // ✅ 도메인 모델(User)만 사용
   public User createUser(String name, String email, int age)

   // ❌ API 모델 사용 안함
   // public UserResponse createUser(CreateUserRequest request)
   ```

2. **UsersApiController 열어보기:**
   ```java
   // ✅ API 모델 받아서
   public ResponseEntity<UserResponse> createUser(CreateUserRequest request) {
       // ✅ 도메인 모델로 변환
       User user = userService.createUser(...);

       // ✅ 다시 API 모델로 변환
       UserResponse response = mapToUserResponse(user);
   }
   ```

---

## 📚 요약

### 핵심 원칙

1. **API 계층 = 명세에서 생성된 모델** 🤖
2. **비즈니스 계층 = 도메인 모델** ✍️
3. **Controller = 변환자** 🔄

### 이점

| 이점 | 설명 |
|------|------|
| 🔒 **독립성** | 비즈니스 로직이 API 변경에 영향받지 않음 |
| ♻️ **재사용** | Service를 다른 API에서도 사용 가능 |
| 🧪 **테스트** | 순수한 비즈니스 로직 테스트 용이 |
| 📝 **명세 준수** | API는 항상 명세와 일치 |

---

**현재 상태: 이미 올바르게 구현되어 있습니다!** ✅

- UserService는 도메인 모델(User)만 사용
- UsersApiController가 변환 담당
- 명세 우선 개발의 모범 사례

계속 이 방식을 유지하시면 됩니다! 🎉