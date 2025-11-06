# RESTful API 완전 정복

REST (Representational State Transfer)는 웹 API를 설계하는 아키텍처 스타일입니다.

## HTTP 메서드 (CRUD 매핑)

| HTTP 메서드 | CRUD | 용도 | 멱등성 | 안전성 |
|------------|------|------|--------|--------|
| **GET** | Read | 조회 | ✅ | ✅ |
| **POST** | Create | 생성 | ❌ | ❌ |
| **PUT** | Update | 전체 수정 | ✅ | ❌ |
| **PATCH** | Update | 부분 수정 | ❌ | ❌ |
| **DELETE** | Delete | 삭제 | ✅ | ❌ |

### 멱등성 (Idempotent)
- 같은 요청을 여러 번 해도 결과가 같음
- GET, PUT, DELETE는 멱등성 보장

### 안전성 (Safe)
- 서버 상태를 변경하지 않음
- GET만 안전함

## HTTP 상태 코드

### 성공 (2xx)
- **200 OK** - 요청 성공 (일반적인 성공)
- **201 Created** - 리소스 생성 성공 (POST)
- **204 No Content** - 성공했지만 반환할 내용 없음 (DELETE)

### 클라이언트 오류 (4xx)
- **400 Bad Request** - 잘못된 요청 (유효성 검증 실패)
- **404 Not Found** - 리소스를 찾을 수 없음
- **409 Conflict** - 리소스 충돌 (중복 이메일 등)

### 서버 오류 (5xx)
- **500 Internal Server Error** - 서버 내부 오류

## RESTful URI 설계 원칙

### 1. 명사 사용, 동사 사용 금지

```
❌ /getUsers          → ✅ GET /users
❌ /createUser        → ✅ POST /users
❌ /updateUser/1      → ✅ PUT /users/1
❌ /deleteUser/1      → ✅ DELETE /users/1
```

### 2. 복수형 사용

```
❌ /user              → ✅ /users
❌ /product           → ✅ /products
```

### 3. 계층 구조 표현

```
/users/{userId}/orders          # 특정 사용자의 주문 목록
/users/{userId}/orders/{orderId} # 특정 사용자의 특정 주문
```

### 4. 소문자 및 하이픈 사용

```
❌ /userProfiles      → ✅ /user-profiles
❌ /User_Profiles     → ✅ /user-profiles
```

## 실전 예제: 사용자 관리 API

### 기본 CRUD

| 동작 | HTTP 메서드 | URI | 상태 코드 |
|------|-------------|-----|-----------|
| 사용자 목록 조회 | GET | `/api/users` | 200 |
| 특정 사용자 조회 | GET | `/api/users/{id}` | 200, 404 |
| 사용자 생성 | POST | `/api/users` | 201, 400 |
| 사용자 전체 수정 | PUT | `/api/users/{id}` | 200, 404 |
| 사용자 부분 수정 | PATCH | `/api/users/{id}` | 200, 404 |
| 사용자 삭제 | DELETE | `/api/users/{id}` | 204, 404 |

### GET - 조회

```bash
# 전체 조회
GET /api/users
→ 200 OK
[{"id":1,"name":"홍길동",...}, {...}]

# 단건 조회
GET /api/users/1
→ 200 OK
{"id":1,"name":"홍길동",...}

# 존재하지 않는 경우
GET /api/users/999
→ 404 Not Found
```

### POST - 생성

```bash
POST /api/users
Content-Type: application/json

{
  "name": "홍길동",
  "email": "hong@example.com",
  "age": 25
}

→ 201 Created
Location: /api/users/1
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "age": 25
}
```

**특징:**
- 요청 본문(body)에 JSON 데이터 전송
- 201 Created 반환
- Location 헤더에 생성된 리소스 URI 포함

### PUT - 전체 수정

```bash
PUT /api/users/1
Content-Type: application/json

{
  "name": "김철수",
  "email": "kim@example.com",
  "age": 30
}

→ 200 OK
{
  "id": 1,
  "name": "김철수",
  "email": "kim@example.com",
  "age": 30
}
```

**특징:**
- 리소스 전체를 교체
- 모든 필드 필수
- 멱등성 보장

### PATCH - 부분 수정

```bash
PATCH /api/users/1
Content-Type: application/json

{
  "age": 31
}

→ 200 OK
{
  "id": 1,
  "name": "김철수",        # 변경 안 됨
  "email": "kim@example.com", # 변경 안 됨
  "age": 31                # 변경됨
}
```

**특징:**
- 일부 필드만 수정
- 제공된 필드만 업데이트

### DELETE - 삭제

```bash
DELETE /api/users/1

→ 204 No Content
(본문 없음)

# 존재하지 않는 경우
DELETE /api/users/999
→ 404 Not Found
```

**특징:**
- 204 No Content 반환 (본문 없음)
- 멱등성 보장 (여러 번 삭제해도 결과 동일)

## Spring Boot에서의 구현

### 1. 기존 방식 (GET 파라미터) - ❌

```java
@GetMapping("/create")
public User create(@RequestParam String name, @RequestParam String email) {
    // 보안 취약, RESTful하지 않음
}
```

### 2. RESTful 방식 - ✅

```java
@PostMapping
public ResponseEntity<User> create(@RequestBody CreateUserRequest request) {
    User user = userService.createUser(request);
    return ResponseEntity
        .created(URI.create("/api/users/" + user.id()))
        .body(user);
}
```

## 주요 어노테이션

### @RequestBody
- HTTP 요청 본문(body)의 JSON을 Java 객체로 변환
- POST, PUT, PATCH에서 사용

```java
@PostMapping("/users")
public User create(@RequestBody CreateUserRequest request) {
    // request 객체에 JSON 데이터가 자동으로 매핑됨
}
```

### @PathVariable
- URI 경로의 변수를 파라미터로 받음

```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    // /users/123 → id = 123
}
```

### @RequestParam
- 쿼리 파라미터를 받음

```java
@GetMapping("/users")
public List<User> search(@RequestParam String keyword) {
    // /users?keyword=홍 → keyword = "홍"
}
```

## Request/Response DTO 패턴

### 왜 DTO를 사용하나요?

```java
// ❌ 나쁜 예: Entity 직접 노출
@PostMapping("/users")
public User create(@RequestBody User user) {
    // User 엔티티의 모든 필드가 노출됨
    // id, createdAt 같은 서버에서 생성할 필드도 클라이언트가 전송 가능
}

// ✅ 좋은 예: DTO 사용
@PostMapping("/users")
public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest request) {
    // 필요한 필드만 받음
    // 응답도 필요한 필드만 반환
}
```

### DTO 예제

```java
// 생성 요청 DTO
public record CreateUserRequest(
    String name,
    String email,
    int age
) {
    // id는 서버에서 생성하므로 포함하지 않음
}

// 수정 요청 DTO
public record UpdateUserRequest(
    String name,
    String email,
    int age
) {}

// 응답 DTO (비밀번호 등 민감한 정보 제외)
public record UserResponse(
    Long id,
    String name,
    String email,
    int age
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.id(),
            user.name(),
            user.email(),
            user.age()
        );
    }
}
```

## 테스트 도구

### curl 사용

```bash
# GET
curl http://localhost:8080/api/users

# POST
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com","age":25}'

# PUT
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"김철수","email":"kim@example.com","age":30}'

# DELETE
curl -X DELETE http://localhost:8080/api/users/1
```

### httpie 사용 (더 간편)

```bash
# POST
http POST localhost:8080/api/users name=홍길동 email=hong@example.com age:=25

# PUT
http PUT localhost:8080/api/users/1 name=김철수 email=kim@example.com age:=30

# DELETE
http DELETE localhost:8080/api/users/1
```

## 핵심 정리

1. **GET** - 조회만, 서버 상태 변경 없음
2. **POST** - 생성, 201 Created 반환
3. **PUT** - 전체 수정, 멱등성 보장
4. **PATCH** - 부분 수정
5. **DELETE** - 삭제, 204 No Content 반환
6. **@RequestBody** - JSON → Java 객체 변환
7. **DTO 사용** - Entity 직접 노출 금지
8. **적절한 HTTP 상태 코드** 사용

다음: 실제 구현 예제는 [UserController.java](src/main/java/com/example/springbasic/controller/UserController.java)를 참고하세요! 🚀