# 세션 컨텍스트 (빠른 참조용)

> Claude Code와의 대화가 끊긴 후 새 세션을 시작할 때 이 파일을 먼저 읽어주세요.

## 현재 프로젝트 상태 (2025-11-06)

### 완성된 기능
✅ **명세 우선 개발 (Spec-First)** 완전 구축
- API: OpenAPI 3.0.3 명세 → 자동 코드 생성
- DB: Liquibase 명세 → 스키마 버전 관리
- 계층 분리: API 모델 ↔ Domain 모델

✅ **RESTful API (7개 엔드포인트)**
- POST `/api/users` - 생성
- GET `/api/users` - 목록
- GET `/api/users/{id}` - 조회
- GET `/api/users/search` - 검색
- PUT `/api/users/{id}` - 전체 수정
- PATCH `/api/users/{id}` - 부분 수정
- DELETE `/api/users/{id}` - 삭제

✅ **DB 연동**
- PostgreSQL 16 (Docker)
- Spring Data JPA
- Liquibase 3개 changeset
- ddl-auto: validate (자동 검증)

### 핵심 아키텍처

```
OpenAPI 명세 → 코드 생성 → Controller 구현
                              ↓
                        API ↔ Domain 변환
                              ↓
                        Service (비즈니스 로직)
                              ↓
                        Repository (JPA)
                              ↓
Liquibase 명세 → DB 스키마 → PostgreSQL
```

### 디렉토리 구조 (중요한 것만)

```
src/main/
├── resources/
│   ├── openapi/               # API 명세 (YAML)
│   │   ├── api-spec.yaml
│   │   ├── paths/
│   │   └── schemas/
│   └── db/changelog/          # DB 명세 (YAML)
│       ├── db.changelog-master.yaml
│       └── changes/
│           ├── 001-create-users-table.yaml
│           ├── 002-add-email-index.yaml
│           └── 003-add-timestamps.yaml
│
└── java/com/example/springbasic/
    ├── controller/
    │   └── UsersApiController.java    # 생성된 UsersApi 구현
    ├── service/
    │   └── UserService.java
    ├── repository/
    │   └── UserRepository.java        # JpaRepository 상속
    └── model/
        └── User.java                  # JPA Entity

build/generated/                       # 자동 생성 (Git 무시)
└── UsersApi.java, DTOs...
```

## 주요 개념 (반드시 기억!)

### 1. 명세 우선 = 코드보다 명세가 먼저!

**API 변경 시:**
1. `api-spec.yaml` 수정
2. `./gradlew generateApi` 실행
3. Controller 구현 수정

**DB 변경 시:**
1. `004-xxx.yaml` changeset 작성
2. `User.java` Entity 수정
3. `./gradlew bootRun` (자동 적용)

### 2. Service는 Domain 모델만 사용!

❌ **잘못된 예:**
```java
@Service
public class UserService {
    public UserResponseDto getUser(Long id) {  // API 모델 사용 금지!
        ...
    }
}
```

✅ **올바른 예:**
```java
@Service
public class UserService {
    public User getUser(Long id) {  // Domain 모델만 사용
        ...
    }
}
```

Controller에서 변환:
```java
@RestController
public class UsersApiController implements UsersApi {
    public ResponseEntity<UserResponseDto> getUser(Long id) {
        User user = userService.getUser(id);  // Domain 모델
        UserResponseDto response = mapToDto(user);  // API 모델로 변환
        return ResponseEntity.ok(response);
    }
}
```

**이유:** [WHY_NOT_API_MODEL_IN_SERVICE.md](WHY_NOT_API_MODEL_IN_SERVICE.md) 참고

### 3. ddl-auto: validate는 안전장치

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 절대 변경 금지!
```

- Entity와 DB 스키마가 불일치하면 시작 실패
- Liquibase로만 스키마 변경
- 프로덕션 안전성 보장

### 4. Liquibase changeset은 영구적

❌ **절대 하지 말 것:**
- 이미 실행된 changeset 수정
- changeset ID 변경
- changeset 삭제

✅ **올바른 방법:**
- 새로운 changeset 추가
- 004, 005, 006... 순차적으로

## 실행 방법

### 처음 시작 시

```bash
# 1. PostgreSQL 시작
docker-compose up -d

# 2. 애플리케이션 실행 (Liquibase 자동 실행)
./gradlew bootRun

# 3. 테스트
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"테스트","email":"test@example.com","age":25}'

curl http://localhost:8080/api/users
```

### 명세 변경 후

```bash
# API 명세 변경 시
./gradlew clean generateApi build

# DB 명세 변경 시
./gradlew bootRun  # 자동으로 새 changeset 실행
```

## 학습 진행 상황

### 완료 (5개 세션)
1. ✅ Spring Boot 기본
2. ✅ Service 레이어
3. ✅ RESTful API
4. ✅ 명세 우선 개발 (API)
5. ✅ 명세 우선 개발 (DB)

### 다음 주제 (우선순위)
1. **유효성 검증** (`@Valid`, Bean Validation)
2. **예외 처리** (`@ControllerAdvice`)
3. **트랜잭션** (`@Transactional`)
4. **테스트** (JUnit, Testcontainers)

## 중요한 문서들

반드시 읽어야 할 문서:
- [README.md](README.md) - 프로젝트 전체 개요
- [LEARNING_LOG.md](LEARNING_LOG.md) - 5개 세션 상세 기록
- [WHY_NOT_API_MODEL_IN_SERVICE.md](WHY_NOT_API_MODEL_IN_SERVICE.md) - Service 계층 설계

참고 문서:
- [SPEC_FIRST_DEVELOPMENT.md](SPEC_FIRST_DEVELOPMENT.md) - 명세 우선 개발
- [DB_SPEC_MANAGEMENT.md](DB_SPEC_MANAGEMENT.md) - Liquibase 사용법
- [VERIFY_DB_MAPPING.md](VERIFY_DB_MAPPING.md) - 매핑 검증 방법

## 새 세션 시작 시 Claude에게 할 말

```
SESSION_CONTEXT.md를 읽어주고, 현재 상태를 파악해줘.
다음 학습 주제는 [원하는 주제]를 하고 싶어.
```

또는

```
LEARNING_LOG.md를 읽어서 지금까지 어떤 학습을 했는지 확인해줘.
```

## 자주 사용하는 명령어

```bash
# PostgreSQL
docker-compose up -d              # 시작
docker-compose ps                 # 상태 확인
docker-compose logs -f postgres   # 로그
docker exec -it springbasic-postgres psql -U springuser -d springbasic  # 접속

# Gradle
./gradlew generateApi             # API 코드 생성
./gradlew clean build             # 클린 빌드
./gradlew bootRun                 # 실행

# API 테스트
curl http://localhost:8080/api/users
curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"name":"홍길동","email":"hong@example.com","age":25}'
```

## 트러블슈팅 체크리스트

### 애플리케이션 시작 안 됨
- [ ] PostgreSQL 실행 중? (`docker-compose ps`)
- [ ] Entity-DB 매핑 에러? (로그 확인)
- [ ] Liquibase changeset 문법 오류?

### API 코드 생성 안 됨
- [ ] `api-spec.yaml` 문법 오류?
- [ ] `./gradlew clean generateApi`로 재생성?
- [ ] `build/generated/` 디렉토리 확인?

### DB 스키마 불일치
- [ ] Liquibase changeset 작성했나?
- [ ] Entity 어노테이션 맞나?
- [ ] VERIFY_DB_MAPPING.md 참고

## 마지막 작업 (이 세션)

오늘 세션에서 한 일:
1. ✅ Liquibase-Entity 매핑 검증 방법 문서 작성 ([VERIFY_DB_MAPPING.md](VERIFY_DB_MAPPING.md))
2. ✅ 학습 기록 업데이트 ([LEARNING_LOG.md](LEARNING_LOG.md))
3. ✅ README 전체 재작성 (명세 우선 개발 반영)
4. ✅ 세션 컨텍스트 문서 작성 (이 파일)

다음 세션에서 할 일:
- 유효성 검증 추가 또는
- 예외 처리 추가 또는
- 실제로 DB 연동 테스트

---

**이 파일은 대화가 끊긴 후 빠르게 맥락을 파악하기 위한 요약본입니다.**
**상세한 내용은 README.md와 LEARNING_LOG.md를 참고하세요.**
