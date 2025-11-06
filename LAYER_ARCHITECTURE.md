# Spring 계층 구조 (Layered Architecture)

실제 Spring 애플리케이션에서 사용하는 **Controller → Service → Repository** 3계층 구조에 대해 배웁니다.

## 계층별 역할

```
┌─────────────────────────────────────┐
│         Controller Layer            │  HTTP 요청/응답 처리
│  @RestController, @Controller       │
└──────────────┬──────────────────────┘
               │ 의존성 주입
               ↓
┌─────────────────────────────────────┐
│          Service Layer              │  비즈니스 로직
│           @Service                  │
└──────────────┬──────────────────────┘
               │ 의존성 주입
               ↓
┌─────────────────────────────────────┐
│        Repository Layer             │  데이터 접근
│         @Repository                 │
└─────────────────────────────────────┘
```

### 1. Controller (컨트롤러)

**역할:**
- HTTP 요청 받기
- 요청 데이터 검증 (간단한 것만)
- Service 호출
- HTTP 응답 반환

**하지 말아야 할 것:**
- ❌ 비즈니스 로직 작성
- ❌ 데이터베이스 직접 접근
- ❌ 복잡한 계산

**예제:** [UserController.java](src/main/java/com/example/springbasic/controller/UserController.java)

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

### 2. Service (서비스)

**역할:**
- 비즈니스 로직 처리
- 트랜잭션 관리
- 데이터 검증
- 여러 Repository 조합
- 복잡한 계산 수행

**하지 말아야 할 것:**
- ❌ HTTP 관련 코드 (Request, Response 등)
- ❌ 데이터베이스 SQL 직접 작성

**예제:** [UserService.java](src/main/java/com/example/springbasic/service/UserService.java)

```java
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String name, String email, int age) {
        // 비즈니스 로직: 중복 이메일 체크
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        User newUser = User.createNew(name, email, age);
        return userRepository.save(newUser);
    }
}
```

### 3. Repository (레포지토리)

**역할:**
- 데이터 저장/조회/수정/삭제
- 데이터베이스와 통신
- 쿼리 실행

**하지 말아야 할 것:**
- ❌ 비즈니스 로직
- ❌ 데이터 검증 (간단한 것 제외)

**예제:** [UserRepository.java](src/main/java/com/example/springbasic/repository/UserRepository.java)

```java
@Repository
public class UserRepository {
    private final Map<Long, User> storage = new ConcurrentHashMap<>();

    public User save(User user) {
        // 데이터 저장 로직만
        storage.put(user.id(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}
```

## 의존성 주입 (Dependency Injection)

Spring의 핵심 기능인 의존성 주입을 통해 계층 간 연결이 이루어집니다.

### 생성자 주입 (권장 방식)

```java
@Service
public class UserService {
    private final UserRepository userRepository;  // final 가능!

    // @Autowired 생략 가능 (생성자가 하나인 경우)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**장점:**
1. ✅ **불변성** - final로 선언 가능
2. ✅ **필수 의존성 보장** - 객체 생성 시 반드시 주입
3. ✅ **테스트 용이** - Mock 객체 주입 쉬움
4. ✅ **순환 참조 방지** - 컴파일 시점 감지

### 필드 주입 (비권장)

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;  // final 불가!
}
```

**단점:**
- ❌ final 사용 불가
- ❌ 테스트 어려움
- ❌ 순환 참조 런타임에만 발견
- ❌ IntelliJ 경고 표시

**결론: 생성자 주입만 사용하세요!**

자세한 예제: [DependencyInjectionExamples.java](src/main/java/com/example/springbasic/examples/DependencyInjectionExamples.java)

## 실습 예제

### 1. 계산기 서비스

간단한 예제로 Service의 개념을 이해합니다.

- Service: [CalculatorService.java](src/main/java/com/example/springbasic/service/CalculatorService.java)
- Controller: [CalculatorController.java](src/main/java/com/example/springbasic/controller/CalculatorController.java)

**테스트:**
```bash
# 덧셈
curl "http://localhost:8080/calculator/add?a=10&b=5"
# 응답: {"operation":"addition","a":10,"b":5,"result":15.0}

# 평균
curl "http://localhost:8080/calculator/average?numbers=10,20,30,40,50"
# 응답: {"average":30.0,"count":5}
```

### 2. 사용자 관리 시스템

실전 예제로 3계층 구조를 모두 사용합니다.

- Model: [User.java](src/main/java/com/example/springbasic/model/User.java)
- Repository: [UserRepository.java](src/main/java/com/example/springbasic/repository/UserRepository.java)
- Service: [UserService.java](src/main/java/com/example/springbasic/service/UserService.java)
- Controller: [UserController.java](src/main/java/com/example/springbasic/controller/UserController.java)

**테스트:**

```bash
# 1. 사용자 생성
curl "http://localhost:8080/api/users/create?name=홍길동&email=hong@example.com&age=25"

# 2. 모든 사용자 조회
curl http://localhost:8080/api/users

# 3. ID로 조회
curl http://localhost:8080/api/users/1

# 4. 성인만 조회 (비즈니스 로직)
curl http://localhost:8080/api/users/adults

# 5. 통계 조회 (비즈니스 로직)
curl http://localhost:8080/api/users/statistics
# 응답: {"totalCount":3,"adultCount":2,"averageAge":24.0,"minAge":17,"maxAge":30}

# 6. 이름으로 검색
curl "http://localhost:8080/api/users/search?keyword=홍"
```

## 계층 분리의 장점

### 1. 관심사의 분리 (Separation of Concerns)
- 각 계층이 자신의 역할에만 집중
- 코드 이해가 쉬움

### 2. 유지보수성
- 비즈니스 로직 변경 → Service만 수정
- 데이터베이스 변경 → Repository만 수정
- API 응답 형식 변경 → Controller만 수정

### 3. 테스트 용이성
```java
// Service 테스트 시 Repository를 Mock으로 대체 가능
@Test
void testCreateUser() {
    UserRepository mockRepo = mock(UserRepository.class);
    UserService service = new UserService(mockRepo);

    // 테스트 진행...
}
```

### 4. 재사용성
- 같은 Service를 여러 Controller에서 사용 가능
- 같은 Repository를 여러 Service에서 사용 가능

### 5. 확장성
- 새로운 기능 추가 시 적절한 계층에만 코드 추가
- 다른 계층에 영향 최소화

## 비즈니스 로직 예제

Service에서 처리하는 비즈니스 로직 예제:

### 1. 데이터 검증
```java
public User createUser(String name, String email, int age) {
    // 중복 이메일 체크
    if (userRepository.findByEmail(email).isPresent()) {
        throw new IllegalArgumentException("이미 존재하는 이메일입니다");
    }
    // ...
}
```

### 2. 데이터 변환/계산
```java
public UserStatistics getUserStatistics() {
    List<User> allUsers = userRepository.findAll();

    double averageAge = allUsers.stream()
            .mapToInt(User::age)
            .average()
            .orElse(0.0);

    return new UserStatistics(averageAge, ...);
}
```

### 3. 필터링
```java
public List<User> getAdultUsers() {
    return userRepository.findAll().stream()
            .filter(user -> user.age() >= 19)
            .toList();
}
```

### 4. 여러 Repository 조합
```java
public Order createOrder(Long userId, List<Long> productIds) {
    User user = userRepository.findById(userId)...;
    List<Product> products = productRepository.findAllById(productIds);

    // 주문 생성 로직...
    return orderRepository.save(order);
}
```

## 다음 단계

Service 레이어를 배웠으니 다음 주제로 넘어갈 수 있습니다:

1. **데이터베이스 연동 (JPA)** - 메모리 저장소를 실제 데이터베이스로 변경
2. **POST/PUT/DELETE 메서드** - RESTful API 완성
3. **예외 처리** - 에러를 우아하게 처리

[NEXT_STEPS.md](NEXT_STEPS.md)를 참고하세요!

## 핵심 정리

1. **Controller**: HTTP 요청/응답만 처리
2. **Service**: 비즈니스 로직 담당 (중요!)
3. **Repository**: 데이터 접근만 담당
4. **의존성 주입**: 생성자 주입 사용
5. **계층 분리**: 유지보수와 테스트가 쉬워짐

이 구조가 Spring의 기본이자 핵심입니다! 🎯