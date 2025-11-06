# 모듈화된 OpenAPI 명세 관리

OpenAPI 명세가 길어지면 가독성이 떨어지므로 **파일을 분리**해서 관리할 수 있습니다!

## 🗂️ 디렉토리 구조

```
src/main/resources/openapi/
├── api-spec.yaml                      # 단일 파일 버전 (기존)
├── api-spec-modular.yaml              # 모듈화 버전 (메인 파일)
│
├── paths/                             # API 경로별 분리
│   ├── users.yaml                     # /api/users 경로
│   ├── users-by-id.yaml              # /api/users/{id} 경로
│   └── statistics.yaml               # /api/users/statistics 경로
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

---

## 📝 파일 분리 예제

### 1. 메인 파일 (api-spec-modular.yaml)

**간결하고 구조가 명확!**

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
  schemas:
    # 외부 파일 참조
    CreateUserRequest:
      $ref: 'schemas/requests/CreateUserRequest.yaml'
    UserResponse:
      $ref: 'schemas/responses/UserResponse.yaml'
```

### 2. 경로 파일 (paths/users.yaml)

**POST, GET 등 메서드별로 정의**

```yaml
post:
  tags:
    - users
  summary: 사용자 생성
  operationId: createUser
  requestBody:
    content:
      application/json:
        schema:
          $ref: '../schemas/requests/CreateUserRequest.yaml'
  responses:
    '201':
      description: 생성 성공
      content:
        application/json:
          schema:
            $ref: '../schemas/responses/UserResponse.yaml'

get:
  tags:
    - users
  summary: 전체 사용자 조회
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

**스키마만 집중해서 작성**

```yaml
type: object
description: 사용자 생성 요청
required:
  - name
  - email
  - age
properties:
  name:
    type: string
    description: 사용자 이름
    example: 홍길동
    minLength: 1
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

## ⚙️ 설정 변경

### build.gradle 수정

모듈화된 명세를 사용하려면 입력 파일만 변경:

```gradle
tasks.register('generateApi', org.openapitools.generator.gradle.plugin.tasks.GenerateTask) {
    generatorName = 'spring'
    // 기존: inputSpec = "$rootDir/src/main/resources/openapi/api-spec.yaml"
    inputSpec = "$rootDir/src/main/resources/openapi/api-spec-modular.yaml"  // 변경!
    outputDir = "$buildDir/generated"
    // ...
}
```

### 코드 생성 테스트

```bash
# 코드 생성
./gradlew generateApi

# 에러 없이 생성되는지 확인
ls build/generated/src/main/java/com/example/springbasic/api/
```

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

**현재 프로젝트:**
- 단일 파일: [api-spec.yaml](src/main/resources/openapi/api-spec.yaml)
- 모듈화 예제: [api-spec-modular.yaml](src/main/resources/openapi/api-spec-modular.yaml)

원하는 방식을 선택해서 사용하세요! 🎉