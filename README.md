# Spring Boot 학습 프로젝트 (명세 우선 개발)

Spring Framework를 명세 우선 개발 방식(Specification-First Development)으로 배우는 학습용 프로젝트입니다.

## 프로젝트 정보

- **Java 버전**: 21
- **Spring Boot 버전**: 3.2.0
- **빌드 도구**: Gradle 8.14.2
- **데이터베이스**: PostgreSQL 16 (Docker)
- **API 명세**: OpenAPI 3.0.3
- **DB 명세**: dbmate
- **포트**: 8080

## 핵심 아키텍처: 명세 우선 개발

이 프로젝트는 **명세가 코드보다 먼저**입니다:

```
┌─────────────────────────────────────────────────────┐
│ API 명세 (OpenAPI)                                   │
│ src/main/resources/openapi/                         │
│ - api-spec.yaml (메인)                              │
│ - paths/ (경로별 명세)                              │
│ - schemas/ (스키마)                                 │
└─────────────────────────────────────────────────────┘
          ↓ OpenAPI Generator
┌─────────────────────────────────────────────────────┐
│ 생성된 코드 (UsersApi, DTOs)                        │
│ build/generated/                                    │
└─────────────────────────────────────────────────────┘
          ↓ 구현
┌─────────────────────────────────────────────────────┐
│ Controller → Service → Repository                   │
│ API 모델 ↔ 도메인 모델 변환                         │
└─────────────────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────────────────┐
│ DB 명세 (dbmate)                                    │
│ database/db/migrations/                             │
│ - 20250101000001_create_users_table.sql             │
│ - 20250101000002_add_email_index.sql                │
│ - 20250101000003_add_updated_at_trigger.sql         │
└─────────────────────────────────────────────────────┘
          ↓ dbmate up
┌─────────────────────────────────────────────────────┐
│ PostgreSQL Database                                 │
│ docker-compose로 실행                               │
└─────────────────────────────────────────────────────┘
```

## 빠른 시작

### 1. PostgreSQL 시작 (자동 마이그레이션 포함!)

```bash
docker-compose up -d
```

**자동 실행:**
- PostgreSQL 시작
- dbmate가 자동으로 DB 스키마 생성/마이그레이션

### 2. API 코드 생성

```bash
./gradlew generateApi
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

**실행 순서:**
1. docker-compose → PostgreSQL + dbmate 자동 마이그레이션
2. JPA가 Entity와 DB 매핑 검증 (`ddl-auto: validate`)
3. Spring Boot 애플리케이션 시작

### 4. API 테스트

```bash
# 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com","age":25}'

# 전체 조회
curl http://localhost:8080/api/users

# ID로 조회
curl http://localhost:8080/api/users/1

# 수정
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com","age":26}'

# 삭제
curl -X DELETE http://localhost:8080/api/users/1
```

## 프로젝트 구조

```
spring_app/
├── README.md                                   # 프로젝트 설명서
├── docker-compose.yml                          # PostgreSQL 설정
├── build.gradle                                # Gradle 빌드 + OpenAPI Generator
│
├── docs/                                       # 프로젝트 문서
│   ├── LEARNING_LOG.md                         # 학습 진행 상황 (5개 세션)
│   ├── SESSION_CONTEXT.md                      # 세션 복구용 컨텍스트
│   ├── NEXT_STEPS.md                           # 다음 학습 단계
│   ├── SPEC_FIRST_DEVELOPMENT.md               # 명세 우선 개발 개념
│   ├── MODULAR_SPEC.md                         # OpenAPI 모듈화
│   ├── LAYER_ARCHITECTURE.md                   # 3계층 구조
│   ├── LAYER_SEPARATION.md                     # 계층 분리
│   ├── WHY_NOT_API_MODEL_IN_SERVICE.md         # API 모델 분리 이유
│   ├── DB_SPEC_MANAGEMENT.md                   # Liquibase 사용법
│   ├── VERIFY_DB_MAPPING.md                    # Entity-DB 매핑 검증
│   ├── API_TEST_GUIDE.md                       # API 테스트 방법
│   ├── RESTFUL_API.md                          # RESTful API 설계
│   └── SPECIFICATION_DRIVEN_DEVELOPMENT.md     # 명세 기반 개발
│
├── src/main/
│   ├── java/com/example/springbasic/
│   │   ├── SpringBasicApplication.java         # 메인 애플리케이션
│   │   ├── controller/
│   │   │   └── UsersApiController.java         # API 구현 (생성된 인터페이스 구현)
│   │   ├── service/
│   │   │   └── UserService.java                # 비즈니스 로직
│   │   ├── repository/
│   │   │   └── UserRepository.java             # JPA Repository
│   │   └── model/
│   │       └── User.java                       # JPA Entity (도메인 모델)
│   │
│   └── resources/
│       ├── application.yml                     # Spring Boot 설정
│       ├── application-dev.yml                 # 개발 환경 설정
│       │
│       ├── openapi/                            # API 명세
│       │   ├── api-spec.yaml                   # 메인 명세
│       │   ├── paths/                          # 경로별 명세
│       │   │   ├── users-base.yaml
│       │   │   ├── users-by-id.yaml
│       │   │   └── users-search.yaml
│       │   └── schemas/                        # 스키마
│       │       ├── CreateUserRequest.yaml
│       │       ├── UpdateUserRequest.yaml
│       │       ├── PatchUserRequest.yaml
│       │       ├── UserResponse.yaml
│       │       └── ErrorResponse.yaml
│       │
│       └── db/changelog/                       # DB 명세
│           ├── db.changelog-master.yaml        # 메인 changeset
│           └── changes/
│               ├── 001-create-users-table.yaml
│               ├── 002-add-email-index.yaml
│               └── 003-add-timestamps.yaml
│
└── build/generated/                            # 자동 생성된 코드
    └── src/main/java/
        └── com/example/springbasic/api/
            ├── UsersApi.java                   # API 인터페이스
            └── model/                          # API 모델
                ├── CreateUserRequestDto.java
                ├── UpdateUserRequestDto.java
                ├── PatchUserRequestDto.java
                └── UserResponseDto.java
```

## 주요 명령어

### 개발 워크플로우

```bash
# 1. API 명세 수정 (api-spec.yaml)
# 2. 코드 생성
./gradlew clean generateApi

# 3. 빌드
./gradlew build

# 4. 실행
./gradlew bootRun

# 5. 테스트
curl http://localhost:8080/api/users
```

### DB 명세 변경 시

```bash
# 1. dbmate 마이그레이션 작성 (20250101000004_xxx.sql)
# 2. Entity 수정 (User.java)
# 3. 마이그레이션 실행
dbmate up
# 4. 애플리케이션 재시작
./gradlew bootRun
```

### PostgreSQL 명령어

```bash
# 시작
docker-compose up -d

# 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f postgres

# 접속
docker exec -it springbasic-postgres psql -U springuser -d springbasic

# DB 내부에서
\dt          # 테이블 목록
\d users     # users 테이블 구조
SELECT * FROM users;           # 데이터 조회
SELECT * FROM schema_migrations;  # dbmate 마이그레이션 이력

# 종료
docker-compose stop

# 완전 삭제 (데이터 포함)
docker-compose down -v
```

## 학습한 핵심 개념

### 1. 명세 우선 개발 (Specification-First)

**Code-First (기존):**
```
코드 작성 → 실행 → 문서 자동 생성
```
- 문서와 코드 불일치 가능

**Spec-First (현재):**
```
명세 작성 → 코드 생성 → 구현
```
- 명세가 항상 정확
- 프론트엔드와 동시 개발 가능
- 계약 기반 개발

### 2. 계층 분리

```
API 계층 (Controller)
  ├─ API 모델 (OpenAPI 생성)
  ├─ CreateUserRequestDto
  └─ UserResponseDto
       ↕ 변환
Domain 계층 (Service, Repository)
  ├─ 도메인 모델 (직접 작성)
  └─ User Entity
```

**왜 분리하나?**
- API 변경이 비즈니스 로직에 영향 없음
- 다른 API(GraphQL 등)에서도 같은 Service 재사용
- 테스트 용이

### 3. JPA + dbmate

**dbmate:**
- DB 스키마 버전 관리
- SQL 마이그레이션으로 변경 이력 Git에 저장
- 롤백 가능

**ddl-auto: validate:**
- Entity와 DB 스키마 자동 검증
- 불일치 시 애플리케이션 시작 실패
- 안전장치

### 4. Spring의 3계층 구조

```
Controller (HTTP 요청/응답)
    ↓
Service (비즈니스 로직)
    ↓
Repository (데이터 접근)
```

## API 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/users` | 사용자 생성 |
| GET | `/api/users` | 전체 조회 |
| GET | `/api/users/{id}` | ID로 조회 |
| GET | `/api/users/search?keyword=xxx` | 이름 검색 |
| PUT | `/api/users/{id}` | 전체 수정 |
| PATCH | `/api/users/{id}` | 부분 수정 |
| DELETE | `/api/users/{id}` | 삭제 |

## 주요 문서

### 학습 가이드
- [LEARNING_LOG.md](docs/LEARNING_LOG.md) - 5개 세션 학습 기록
- [SESSION_CONTEXT.md](docs/SESSION_CONTEXT.md) - 세션 복구용 컨텍스트
- [NEXT_STEPS.md](docs/NEXT_STEPS.md) - 다음 학습 단계

### API 명세
- [SPEC_FIRST_DEVELOPMENT.md](docs/SPEC_FIRST_DEVELOPMENT.md) - 명세 우선 개발 개념
- [MODULAR_SPEC.md](docs/MODULAR_SPEC.md) - OpenAPI 모듈화
- [API_TEST_GUIDE.md](docs/API_TEST_GUIDE.md) - API 테스트 방법

### 아키텍처
- [LAYER_SEPARATION.md](docs/LAYER_SEPARATION.md) - 계층 분리
- [WHY_NOT_API_MODEL_IN_SERVICE.md](docs/WHY_NOT_API_MODEL_IN_SERVICE.md) - Service에서 API 모델 사용하지 않는 이유
- [LAYER_ARCHITECTURE.md](docs/LAYER_ARCHITECTURE.md) - 3계층 구조

### DB 관리
- [DB_SPEC_MANAGEMENT.md](docs/DB_SPEC_MANAGEMENT.md) - dbmate 사용법
- [VERIFY_DB_MAPPING.md](docs/VERIFY_DB_MAPPING.md) - Entity-DB 매핑 검증

### 기타
- [RESTFUL_API.md](docs/RESTFUL_API.md) - RESTful API 설계
- [SPECIFICATION_DRIVEN_DEVELOPMENT.md](docs/SPECIFICATION_DRIVEN_DEVELOPMENT.md) - 명세 기반 개발

## 학습 진행 상황

### 완료한 레벨

- ✅ **레벨 1**: Spring Boot 기본
- ✅ **레벨 2**: Service 레이어
- ✅ **레벨 3**: RESTful API
- ✅ **레벨 4**: 명세 우선 개발 (API)
- ✅ **레벨 5**: 명세 우선 개발 (DB)

### 학습한 기술 스택

**백엔드:**
- Spring Boot 3.2.0
- Spring Data JPA
- PostgreSQL 16

**API 명세:**
- OpenAPI 3.0.3
- OpenAPI Generator

**DB 명세:**
- dbmate

**인프라:**
- Docker Compose

### 다음 학습 주제

1. **유효성 검증** - `@Valid`, `@NotNull` 등
2. **예외 처리** - `@ControllerAdvice`로 전역 처리
3. **트랜잭션** - `@Transactional`로 데이터 일관성 보장
4. **테스트** - JUnit + Testcontainers

## Claude Code로 계속 학습하기

세션이 끊긴 후에도 계속 학습하려면:

### 1. 이전 학습 내용 확인

```
docs/LEARNING_LOG.md 파일을 읽어줘
```

### 2. 현재 상태 확인

```
README.md와 docs/SESSION_CONTEXT.md를 읽어줘
```

### 3. 다음 단계 진행

```
유효성 검증을 추가하고 싶어. @Valid를 사용해서 입력 검증을 하자
```

```
예외 처리를 추가하고 싶어. @ControllerAdvice로 전역 예외 처리를 만들자
```

## 문제 해결

### PostgreSQL 연결 실패

```bash
# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs postgres

# 재시작
docker-compose restart postgres
```

### Entity-DB 매핑 불일치

```bash
# 애플리케이션 실행 시 에러 메시지 확인
./gradlew bootRun

# 예시: "Schema-validation: missing column [phone_number]"
# → dbmate에 컬럼 추가 마이그레이션 작성 필요
```

### OpenAPI 코드 생성 실패

```bash
# 클린 빌드
./gradlew clean generateApi build

# generated 디렉토리 확인
ls -la build/generated/src/main/java/com/example/springbasic/api/
```

## 프로젝트 정보

- **생성일**: 2025-11-06
- **마지막 업데이트**: 2025-11-06
- **학습 도구**: Claude Code
- **목적**: Spring Framework 명세 우선 개발 학습
- **총 학습 세션**: 5회