# 명세 우선 개발 (Spec-First Development)

이 프로젝트는 **명세 우선 개발** 방식으로 전환되었습니다!

## 🎯 핵심 개념

### 코드 우선 vs 명세 우선

| 방식 | 워크플로우 | 장점 | 단점 |
|------|-----------|------|------|
| **코드 우선**<br>(Code-First) | Java 코드 작성 → 명세 자동 생성 | 빠른 개발, 자동 동기화 | 설계보다 구현 먼저 |
| **명세 우선**<br>(Spec-First) | YAML 명세 작성 → 코드 자동 생성 | 설계 먼저, API 계약 명확 | 초기 설정 복잡 |

**이 프로젝트는 명세 우선 방식을 사용합니다!**

---

## 📁 프로젝트 구조

```
spring_app/
├── src/main/resources/openapi/
│   └── api-spec.yaml                    ⭐ 명세 파일 (시작점!)
├── build/generated/src/main/java/
│   └── com/example/springbasic/api/
│       ├── UsersApi.java                 🤖 자동 생성된 인터페이스
│       └── model/
│           ├── CreateUserRequest.java    🤖 자동 생성된 모델
│           ├── UpdateUserRequest.java    🤖 자동 생성된 모델
│           ├── PatchUserRequest.java     🤖 자동 생성된 모델
│           ├── UserResponse.java         🤖 자동 생성된 모델
│           └── UserStatistics.java       🤖 자동 생성된 모델
└── src/main/java/.../controller/
    └── UsersApiController.java           ✍️ 개발자가 작성 (구현)
```

### 파일별 역할

| 파일 | 누가 작성? | 설명 |
|------|----------|------|
| [api-spec.yaml](src/main/resources/openapi/api-spec.yaml) | ✍️ **개발자** | API 명세 정의 (가장 중요!) |
| UsersApi.java | 🤖 **자동 생성** | 컨트롤러가 구현할 인터페이스 |
| model/*.java | 🤖 **자동 생성** | 요청/응답 DTO 클래스 |
| [UsersApiController.java](src/main/java/com/example/springbasic/controller/UsersApiController.java) | ✍️ **개발자** | 인터페이스 구현 |

---

## 🚀 개발 워크플로우

### 1단계: 명세 작성 (가장 중요!)

[api-spec.yaml](src/main/resources/openapi/api-spec.yaml)을 수정합니다:

```yaml
paths:
  /api/users:
    post:
      summary: 사용자 생성
      operationId: createUser
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUserRequest'
      responses:
        '201':
          description: 사용자 생성 성공
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'

components:
  schemas:
    CreateUserRequest:
      type: object
      required:
        - name
        - email
        - age
      properties:
        name:
          type: string
          description: 사용자 이름
          example: 홍길동
        email:
          type: string
          format: email
          example: hong@example.com
        age:
          type: integer
          minimum: 0
          maximum: 150
          example: 25
```

### 2단계: 코드 생성

명세에서 Java 코드를 자동 생성합니다:

```bash
./gradlew generateApi
```

**생성되는 것:**
- `UsersApi.java` - 인터페이스 (메서드 시그니처)
- `model/*.java` - 요청/응답 클래스

### 3단계: 생성된 코드 확인

```bash
# 생성된 인터페이스 확인
cat build/generated/src/main/java/com/example/springbasic/api/UsersApi.java

# 생성된 모델 확인
ls build/generated/src/main/java/com/example/springbasic/api/model/
```

### 4단계: 인터페이스 구현

[UsersApiController.java](src/main/java/com/example/springbasic/controller/UsersApiController.java)에서 구현:

```java
@RestController
public class UsersApiController implements UsersApi {

    private final UserService userService;

    @Override
    public ResponseEntity<UserResponse> createUser(
            CreateUserRequest createUserRequest
    ) {
        // 비즈니스 로직 구현
        User user = userService.createUser(
            createUserRequest.getName(),
            createUserRequest.getEmail(),
            createUserRequest.getAge()
        );

        // 응답 생성
        UserResponse response = mapToUserResponse(user);
        return ResponseEntity.created(...)
                             .body(response);
    }
}
```

### 5단계: 빌드 및 실행

```bash
# 빌드 (자동으로 generateApi 실행됨)
./gradlew build

# 실행
./gradlew bootRun
```

### 6단계: 명세 확인 및 테스트

브라우저에서 확인:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **명세 JSON**: http://localhost:8080/v3/api-docs
- **명세 YAML**: http://localhost:8080/v3/api-docs.yaml

---

## 🔄 변경 시나리오

### 시나리오 1: 새로운 필드 추가

**요구사항**: 사용자에게 "전화번호" 필드 추가

#### 1️⃣ 명세 수정 (api-spec.yaml)

```yaml
components:
  schemas:
    CreateUserRequest:
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

#### 3️⃣ 컴파일 에러 확인

생성된 인터페이스가 변경되어 컴파일 에러 발생! (좋은 신호!)

```
UsersApiController.java:35: error: method does not override or implement a method from a supertype
```

#### 4️⃣ 구현 수정

[UsersApiController.java](src/main/java/com/example/springbasic/controller/UsersApiController.java)에서:

```java
User user = userService.createUser(
    createUserRequest.getName(),
    createUserRequest.getEmail(),
    createUserRequest.getAge(),
    createUserRequest.getPhoneNumber()  // 새 필드 추가
);
```

#### 5️⃣ Service/Repository도 수정

도메인 모델과 비즈니스 로직도 변경합니다.

---

### 시나리오 2: 새로운 API 추가

**요구사항**: 이메일로 사용자 검색 API

#### 1️⃣ 명세에 엔드포인트 추가

```yaml
paths:
  /api/users/search/email:
    get:
      summary: 이메일로 사용자 조회
      operationId: getUserByEmail
      parameters:
        - name: email
          in: query
          required: true
          schema:
            type: string
            format: email
      responses:
        '200':
          description: 조회 성공
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'
        '404':
          description: 사용자를 찾을 수 없음
```

#### 2️⃣ 코드 재생성

```bash
./gradlew generateApi
```

#### 3️⃣ 새 메서드 구현

UsersApi 인터페이스에 `getUserByEmail()` 메서드가 추가됨!

[UsersApiController.java](src/main/java/com/example/springbasic/controller/UsersApiController.java)에서 구현:

```java
@Override
public ResponseEntity<UserResponse> getUserByEmail(String email) {
    return userService.getUserByEmail(email)
        .map(this::mapToUserResponse)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
}
```

---

## 📊 명세 작성 팁

### 필수 요소

```yaml
openapi: 3.0.1
info:
  title: API 제목
  version: 1.0.0

paths:
  /api/resource:
    get:
      operationId: getResource    # ⭐ 필수! 메서드 이름이 됨
      responses:
        '200':
          description: 성공

components:
  schemas:
    ResourceResponse:
      type: object
      properties:
        id:
          type: integer
```

### 상세한 설명 추가

```yaml
paths:
  /api/users:
    post:
      summary: 사용자 생성         # 짧은 요약
      description: |               # 상세 설명 (마크다운 사용 가능)
        새로운 사용자를 생성합니다.

        **제약사항:**
        - 이메일은 중복될 수 없습니다
        - 나이는 0 이상이어야 합니다
      tags:
        - users                    # Swagger UI에서 그룹화
```

### 예제 추가

```yaml
components:
  schemas:
    CreateUserRequest:
      type: object
      properties:
        name:
          type: string
          example: 홍길동          # 예제 값
          minLength: 1            # 검증 규칙
        age:
          type: integer
          minimum: 0              # 최소값
          maximum: 150            # 최대값
```

---

## 🛠 자주 사용하는 명령어

```bash
# 코드 생성
./gradlew generateApi

# 생성된 파일 확인
ls build/generated/src/main/java/com/example/springbasic/api/

# 빌드 (자동으로 generateApi 실행)
./gradlew build

# 클린 빌드
./gradlew clean build

# 실행
./gradlew bootRun

# 명세 확인
curl http://localhost:8080/v3/api-docs
```

---

## 💡 장점과 단점

### ✅ 장점

1. **명세가 먼저**
   - 구현 전에 API 설계 검토 가능
   - 팀원과 API 계약 합의 후 개발 시작

2. **타입 안전성**
   - 명세 변경 시 컴파일 에러로 감지
   - 실수로 누락되는 필드 없음

3. **병렬 개발**
   - 프론트엔드: 명세만 보고 개발 시작
   - 백엔드: 명세대로 구현

4. **문서 자동화**
   - Swagger UI 자동 생성
   - 항상 최신 상태 유지

5. **클라이언트 코드 생성**
   - 같은 명세로 TypeScript, Kotlin 등 클라이언트 코드도 생성 가능

### ❌ 단점

1. **초기 설정 복잡**
   - OpenAPI Generator 플러그인 설정
   - Gradle 태스크 구성

2. **학습 곡선**
   - OpenAPI 스펙 문법 학습 필요
   - YAML 작성 숙련 필요

3. **빌드 시간 증가**
   - 매번 코드 생성 단계 추가

4. **생성 코드 제어 어려움**
   - 자동 생성 코드를 직접 수정할 수 없음
   - 명세를 바꿔야 함

---

## 🎓 학습 자료

### OpenAPI 스펙 배우기

- [OpenAPI Specification](https://swagger.io/specification/)
- [OpenAPI 공식 가이드](https://learn.openapis.org/)

### 도구

- [Swagger Editor](https://editor.swagger.io/) - 온라인 명세 편집기
- [OpenAPI Generator](https://openapi-generator.tech/) - 코드 생성 도구

---

## 📝 요약

### 명세 우선 개발의 핵심

```
1. api-spec.yaml 작성 (YAML)
   ↓
2. ./gradlew generateApi (코드 생성)
   ↓
3. UsersApiController 구현 (Java)
   ↓
4. ./gradlew bootRun (실행)
   ↓
5. Swagger UI로 테스트
```

### 가장 중요한 파일

1. **[api-spec.yaml](src/main/resources/openapi/api-spec.yaml)** - 모든 것의 시작
2. **[UsersApiController.java](src/main/java/com/example/springbasic/controller/UsersApiController.java)** - 실제 구현

### 핵심 원칙

> **"명세가 곧 계약이다"**
>
> 명세를 변경하면 → 인터페이스가 변경되고 → 컴파일 에러가 발생하며 → 구현을 강제한다

---

## 🔗 관련 문서

- [API_TEST_GUIDE.md](API_TEST_GUIDE.md) - API 테스트 방법
- [LAYER_ARCHITECTURE.md](LAYER_ARCHITECTURE.md) - 계층 구조 설명
- [build.gradle](build.gradle) - OpenAPI Generator 설정

---

**이제 명세를 먼저 작성하고, 코드는 자동으로 생성하세요!** 🎉