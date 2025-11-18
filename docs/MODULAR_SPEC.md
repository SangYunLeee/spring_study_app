# 모듈화된 OpenAPI 명세 관리

OpenAPI 명세가 길어지면 가독성이 떨어지므로 **파일을 분리**해서 관리할 수 있습니다!

## 🗂️ 디렉토리 구조

**현재 프로젝트는 이미 모듈화되어 있습니다!**

```
src/main/resources/openapi/
├── api-spec.yaml                      # 📌 메인 진입점 (모듈화됨!)
│
├── paths/                             # API 경로별 분리
│   ├── users.yaml                     # POST /api/users, GET /api/users
│   ├── users-by-id.yaml              # GET/PUT/PATCH/DELETE /api/users/{id}
│   ├── users-search.yaml             # GET /api/users/search
│   ├── users-adults.yaml             # GET /api/users/adults
│   └── users-statistics.yaml         # GET /api/users/statistics
│
└── schemas/                           # 스키마별 분리
    ├── requests/                      # 요청 스키마
    │   ├── CreateUserRequest.yaml
    │   ├── UpdateUserRequest.yaml
    │   └── PatchUserRequest.yaml
    └── responses/                     # 응답 스키마
        ├── UserResponse.yaml
        ├── UserStatistics.yaml
        └── ErrorResponse.yaml
```

**api-spec.yaml이 $ref로 외부 파일을 참조하므로 이미 모듈화된 구조입니다!**

---

## 📝 파일 분리 예제

### 1. 메인 파일 (api-spec.yaml)

**현재 프로젝트의 실제 구조 - 간결하고 명확!**

```yaml
openapi: 3.0.1
info:
  title: 사용자 관리 API
  version: 1.0.0

paths:
  # 외부 파일 참조
  /api/users:
    $ref: 'paths/users.yaml'

  /api/users/{id}:
    $ref: 'paths/users-by-id.yaml'

components:
  # 재사용 가능한 파라미터
  parameters:
    UserId:
      name: id
      in: path
      description: 사용자 고유 ID
      required: true
      schema:
        type: integer
        format: int64

  schemas:
    # 외부 파일 참조
    CreateUserRequest:
      $ref: 'schemas/requests/CreateUserRequest.yaml'
    UserResponse:
      $ref: 'schemas/responses/UserResponse.yaml'
    ErrorResponse:
      $ref: 'schemas/responses/ErrorResponse.yaml'

  # 재사용 가능한 응답
  responses:
    BadRequest:
      description: 잘못된 요청
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    NotFound:
      description: 리소스를 찾을 수 없음
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
```

### 2. 경로 파일 (paths/users.yaml)

**POST, GET 등 메서드별로 정의 + 공통 응답 재사용**

```yaml
post:
  tags:
    - users
  summary: 사용자 생성
  operationId: createUser
  requestBody:
    description: 생성할 사용자 정보
    required: true
    content:
      application/json:
        schema:
          $ref: '../schemas/requests/CreateUserRequest.yaml'
        examples:
          general:
            summary: 일반 사용자 생성
            value:
              name: 홍길동
              email: hong@example.com
              age: 25
  responses:
    '201':
      description: 사용자 생성 성공
      headers:
        Location:
          description: 생성된 리소스의 URI
          schema:
            type: string
            example: /api/users/1
      content:
        application/json:
          schema:
            $ref: '../schemas/responses/UserResponse.yaml'
    '400':
      $ref: '../api-spec.yaml#/components/responses/BadRequest'

get:
  tags:
    - users
  summary: 전체 사용자 목록 조회
  operationId: getAllUsers
  responses:
    '200':
      description: 조회 성공
      content:
        application/json:
          schema:
            type: array
            items:
              $ref: '../schemas/responses/UserResponse.yaml'
```

### 3. 스키마 파일 (schemas/requests/CreateUserRequest.yaml)

**스키마만 집중해서 작성 + 상세한 제약사항**

```yaml
type: object
description: |
  사용자 생성 요청 (POST /api/users)

  모든 필드가 필수입니다.
required:
  - name
  - email
  - age
properties:
  name:
    type: string
    description: 사용자 이름 (최소 1자 이상)
    minLength: 1
    maxLength: 100
    example: 홍길동
  email:
    type: string
    description: |
      이메일 주소 (로그인 ID로 사용)

      **제약사항:**
      - 이메일 형식이어야 함
      - 시스템 내에서 중복 불가 (유니크)
    format: email
    maxLength: 255
    example: hong@example.com
  age:
    type: integer
    description: 나이 (0~150 사이)
    format: int32
    minimum: 0
    maximum: 150
    example: 25
```

---

## 🔗 $ref 사용법

### 상대 경로 참조

```yaml
# 메인 파일에서 하위 디렉토리 참조
components:
  schemas:
    UserResponse:
      $ref: 'schemas/responses/UserResponse.yaml'

# paths 파일에서 schemas 참조 (상위로 올라가기)
schema:
  $ref: '../schemas/responses/UserResponse.yaml'
```

### 경로 규칙

| 위치 | 참조 경로 | 예시 |
|------|---------|------|
| 같은 디렉토리 | `파일명.yaml` | `UserResponse.yaml` |
| 하위 디렉토리 | `폴더/파일명.yaml` | `schemas/UserResponse.yaml` |
| 상위 디렉토리 | `../파일명.yaml` | `../UserResponse.yaml` |
| 2단계 상위 | `../../파일명.yaml` | `../../common.yaml` |

---

## ✅ 장점

### 1. **가독성 향상**
- 각 파일이 짧고 집중적
- 원하는 부분만 쉽게 찾기

### 2. **재사용성**
```yaml
# 여러 API에서 같은 스키마 재사용
/api/users:
  get:
    responses:
      '200':
        content:
          application/json:
            schema:
              $ref: 'schemas/UserResponse.yaml'

/api/admin/users:
  get:
    responses:
      '200':
        content:
          application/json:
            schema:
              $ref: 'schemas/UserResponse.yaml'  # 같은 스키마!
```

### 3. **협업 용이**
- 개발자마다 다른 파일 수정 → Git 충돌 감소
- 리뷰 시 변경된 파일만 집중

### 4. **유지보수**
- 스키마 변경 시 한 곳만 수정
- 영향 범위 파악 쉬움

---

## ⚙️ 설정 (현재 프로젝트)

### build.gradle 설정

**현재 프로젝트는 이미 모듈화된 api-spec.yaml을 사용 중입니다:**

```gradle
tasks.register('generateApi', org.openapitools.generator.gradle.plugin.tasks.GenerateTask) {
    generatorName = 'spring'
    inputSpec = "$rootDir/src/main/resources/openapi/api-spec.yaml"  // 이미 모듈화됨!
    outputDir = "$buildDir/generated"
    // ...
}
```

### 코드 생성 테스트

```bash
# 코드 생성
./gradlew clean generateApi

# 생성된 API 인터페이스 확인
ls build/generated/src/main/java/com/example/springbasic/api/
# UsersApi.java (생성됨)

# 생성된 모델 확인
ls build/generated/src/main/java/com/example/springbasic/model/
# CreateUserRequest.java, UserResponse.java 등
```

**OpenAPI Generator는 $ref를 자동으로 해석하여 하나의 통합 명세로 처리합니다!**

---

## 📚 조직 전략

### 전략 1: 경로 중심 (Path-Centric)

**도메인별로 경로 파일 분리**

```
paths/
├── users/
│   ├── collection.yaml      # /api/users (POST, GET)
│   ├── item.yaml            # /api/users/{id}
│   └── search.yaml          # /api/users/search
├── products/
│   ├── collection.yaml
│   └── item.yaml
└── orders/
    └── collection.yaml
```

### 전략 2: 기능 중심 (Feature-Centric)

**기능별로 전체 명세 분리**

```
openapi/
├── users/
│   ├── api-spec.yaml        # 사용자 관련 전체 명세
│   ├── paths.yaml
│   └── schemas.yaml
├── products/
│   └── api-spec.yaml
└── orders/
    └── api-spec.yaml
```

### 전략 3: 계층 중심 (Layer-Centric) ⭐ 추천

**요청/응답/공통으로 분리**

```
schemas/
├── requests/                # 모든 요청 스키마
│   ├── CreateUserRequest.yaml
│   ├── CreateProductRequest.yaml
│   └── ...
├── responses/               # 모든 응답 스키마
│   ├── UserResponse.yaml
│   ├── ProductResponse.yaml
│   └── ...
└── common/                  # 공통 스키마
    ├── Pagination.yaml
    ├── ErrorResponse.yaml
    └── Timestamp.yaml
```

---

## 🎯 실전 예제

### 전체 프로젝트 구조

```
src/main/resources/openapi/
├── api-spec.yaml                          # 📌 메인 진입점
│
├── info.yaml                              # API 정보 (title, version 등)
│
├── paths/
│   ├── users/
│   │   ├── collection.yaml                # POST /api/users, GET /api/users
│   │   ├── item.yaml                      # GET/PUT/DELETE /api/users/{id}
│   │   ├── search.yaml                    # GET /api/users/search
│   │   └── statistics.yaml                # GET /api/users/statistics
│   └── health/
│       └── check.yaml                     # GET /health
│
├── schemas/
│   ├── requests/
│   │   ├── users/
│   │   │   ├── CreateUserRequest.yaml
│   │   │   ├── UpdateUserRequest.yaml
│   │   │   └── PatchUserRequest.yaml
│   │   └── common/
│   │       └── SearchRequest.yaml
│   │
│   ├── responses/
│   │   ├── users/
│   │   │   ├── UserResponse.yaml
│   │   │   └── UserStatistics.yaml
│   │   └── common/
│   │       ├── ErrorResponse.yaml
│   ��       └── PageResponse.yaml
│   │
│   └── models/                            # 도메인 모델
│       └── User.yaml
│
└── components/
    ├── parameters/                        # 재사용 가능한 파라미터
    │   ├── PathUserId.yaml
    │   └── QueryKeyword.yaml
    └── headers/                           # 재사용 가능한 헤더
        └── Location.yaml
```

### 메인 파일 (api-spec.yaml)

```yaml
openapi: 3.0.1

# 외부 파일로 분리된 정보
info:
  $ref: 'info.yaml'

servers:
  - url: http://localhost:8080
    description: 로컬 개발 서버

tags:
  - name: users
    description: 사용자 관리

paths:
  # 사용자 관련
  /api/users:
    $ref: 'paths/users/collection.yaml'
  /api/users/{id}:
    $ref: 'paths/users/item.yaml'
  /api/users/search:
    $ref: 'paths/users/search.yaml'
  /api/users/statistics:
    $ref: 'paths/users/statistics.yaml'

  # 헬스 체크
  /health:
    $ref: 'paths/health/check.yaml'

components:
  schemas:
    # 요청
    CreateUserRequest:
      $ref: 'schemas/requests/users/CreateUserRequest.yaml'
    UpdateUserRequest:
      $ref: 'schemas/requests/users/UpdateUserRequest.yaml'

    # 응답
    UserResponse:
      $ref: 'schemas/responses/users/UserResponse.yaml'
    ErrorResponse:
      $ref: 'schemas/responses/common/ErrorResponse.yaml'

  parameters:
    PathUserId:
      $ref: 'components/parameters/PathUserId.yaml'
```

---

## 🚨 주의사항

### 1. **순환 참조 금지**

❌ **잘못된 예:**
```yaml
# UserResponse.yaml
type: object
properties:
  friends:
    type: array
    items:
      $ref: 'UserResponse.yaml'  # 자기 자신 참조!
```

✅ **올바른 예:**
```yaml
# UserResponse.yaml
type: object
properties:
  friends:
    type: array
    items:
      $ref: '#/components/schemas/UserResponse'  # 메인 파일에서 정의
```

### 2. **경로 일관성 유지**

모든 파일에서 일관된 경로 규칙 사용:
- `schemas/` 로 시작 (O)
- `./schemas/` 로 시작 (X)

### 3. **파일명 규칙**

- PascalCase: `UserResponse.yaml` ✅
- kebab-case: `user-response.yaml` (선호도에 따라)
- snake_case: `user_response.yaml` (비추천)

---

## 🔄 단일 파일 ↔ 모듈화 전환

### 모듈화로 전환

```bash
# 1. 디렉토리 생성
mkdir -p src/main/resources/openapi/{paths,schemas/{requests,responses}}

# 2. 스키마 분리 (수동으로 복사/붙여넣기)

# 3. build.gradle 수정
# inputSpec 경로 변경

# 4. 테스트
./gradlew generateApi
```

### 단일 파일로 병합

OpenAPI Generator는 자동으로 `$ref`를 해석하므로 걱정 없음!

---

## 📖 참고 자료

- [OpenAPI $ref 가이드](https://swagger.io/docs/specification/using-ref/)
- [OpenAPI 모범 사례](https://swagger.io/resources/articles/best-practices-in-api-design/)

---

## ✨ 요약

| 항목 | 단일 파일 | 모듈화 |
|------|----------|--------|
| **가독성** | 파일이 길어지면 떨어짐 | 항상 좋음 |
| **재사용** | 어려움 | 쉬움 |
| **협업** | Git 충돌 많음 | 충돌 적음 |
| **초기 설정** | 간단 | 복잡 |
| **유지보수** | 어려움 | 쉬움 |

**권장:** 프로젝트가 작을 때는 단일 파일, 커지면 모듈화!

---

## 🎯 현재 프로젝트 상태

**이미 모듈화된 구조를 사용 중입니다!** ✅

- **메인 파일**: [api-spec.yaml](../src/main/resources/openapi/api-spec.yaml)
- **경로 파일**: [paths/](../src/main/resources/openapi/paths/)
  - [users.yaml](../src/main/resources/openapi/paths/users.yaml)
  - [users-by-id.yaml](../src/main/resources/openapi/paths/users-by-id.yaml)
- **스키마 파일**: [schemas/](../src/main/resources/openapi/schemas/)
  - [requests/CreateUserRequest.yaml](../src/main/resources/openapi/schemas/requests/CreateUserRequest.yaml)
  - [responses/UserResponse.yaml](../src/main/resources/openapi/schemas/responses/UserResponse.yaml)
  - [responses/ErrorResponse.yaml](../src/main/resources/openapi/schemas/responses/ErrorResponse.yaml)

**특징:**
- ✅ 외부 파일 참조 (`$ref`)
- ✅ 공통 컴포넌트 재사용 (`components.parameters`, `components.responses`)
- ✅ 상세한 설명과 풍부한 예제
- ✅ 일관된 디렉토리 구조

**새로운 API 추가 시:**
1. `paths/` 폴더에 새 파일 생성
2. `api-spec.yaml`의 `paths:` 섹션에 참조 추가
3. `./gradlew generateApi` 실행

🎉