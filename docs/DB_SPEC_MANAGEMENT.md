# DB 명세 관리 (DBML + dbmate + PostgreSQL)

**DB 스키마는 DBML 명세로 시작하여 애플리케이션과 독립적으로 관리됩니다!**

API는 OpenAPI로, DB는 DBML(시각화 가능)로 명세를 관리합니다.

## 🎯 전체 구조

```
┌─────────────────────────────────────────────────────────┐
│        API 명세 (OpenAPI)                                │
│        src/main/resources/openapi/api-spec.yaml          │
└─────────────────────────────────────────────────────────┘
                      ↓ 코드 생성
┌─────────────────────────────────────────────────────────┐
│        Controller (UsersApiController)                   │
│        API 모델 ↔ 도메인 모델 변환                        │
└─────────────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────┐
│        Service (UserService)                             │
│        비즈니스 로직                                      │
└─────────────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────┐
│        Domain Entity (User)                              │
│        JPA @Entity                                       │
└─────────────────────────────────────────────────────────┘
                      ↓ (독립적!)
┌─────────────────────────────────────────────────────────┐
│        DB 명세 (DBML) ⭐                                  │
│        database/schema.dbml                              │
│        - dbdiagram.io에서 시각화 가능!                   │
└─────────────────────────────────────────────────────────┘
                      ↓ DBML → SQL 변환
┌─────────────────────────────────────────────────────────┐
│        dbmate 마이그레이션                                │
│        database/db/migrations/ (순수 SQL)                │
│        - 20250101000001_create_users_table.sql           │
│        - 20250101000002_add_email_index.sql              │
│        - 20250101000003_add_updated_at_trigger.sql       │
└─────────────────────────────────────────────────────────┘
                      ↓ 스키마 적용 (독립 실행!)
┌─────────────────────────────────────────────────────────┐
│        PostgreSQL Database                               │
│        docker-compose로 실행                             │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 빠른 시작

### 0. 개념 이해

```
schema.dbml (명세) → SQL 생성 → dbmate 마이그레이션 → PostgreSQL
```

### 1. 도구 설치

#### DBML CLI (명세 관리)

```bash
cd database

# 설치
./scripts/install-dbml-cli.sh

# 또는
npm install -g @dbml/cli

# 확인
dbml2sql --version
```

#### dbmate (마이그레이션 실행)

```bash
# Mac (Homebrew)
brew install dbmate

# Mac (수동 설치)
curl -fsSL -o ~/bin/dbmate https://github.com/amacneil/dbmate/releases/latest/download/dbmate-macos-arm64
chmod +x ~/bin/dbmate

# Linux
curl -fsSL -o /usr/local/bin/dbmate https://github.com/amacneil/dbmate/releases/latest/download/dbmate-linux-amd64
chmod +x /usr/local/bin/dbmate

# 확인
dbmate --version
```

### 2. PostgreSQL 시작

```bash
# Docker Compose로 PostgreSQL 시작
docker-compose up -d

# 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs postgres
```

### 3. DB 마이그레이션 실행

```bash
cd database

# 모든 마이그레이션 적용
dbmate up

# 상태 확인
dbmate status
```

### 4. 애플리케이션 실행

```bash
cd ..

# Spring Boot 시작 (JPA가 스키마 검증만!)
./gradlew bootRun
```

**실행 순서:**
1. **독립적으로** dbmate로 DB 스키마 생성/변경
2. Spring Boot 시작
3. JPA가 스키마 검증 (validate)
4. 애플리케이션 시작 완료

---

## 📁 디렉토리 구조

```
spring_app/
├── database/                           # DB 관리 (독립 프로젝트!)
│   ├── .env                            # DB 연결 설정
│   ├── README.md                       # DB 관리 가이드
│   └── db/
│       ├── migrations/                 # 마이그레이션 파일 (순수 SQL)
│       │   ├── 20250101000001_create_users_table.sql
│       │   ├── 20250101000002_add_email_index.sql
│       │   └── 20250101000003_add_updated_at_trigger.sql
│       └── schema.sql                  # 현재 스키마 (자동 생성)
│
├── src/main/
│   ├── java/                           # 백엔드 소스
│   └── resources/
│       ├── application.yml             # Spring 설정 (Liquibase 없음!)
│       └── openapi/                    # API 명세
│
└── docker-compose.yml                  # PostgreSQL
```

---

## 📝 마이그레이션 파일 구조

### 파일 이름 규칙

```
YYYYMMDDHHMMSS_description.sql
예: 20250101000001_create_users_table.sql
```

### 마이그레이션 파일 내용

```sql
-- migrate:up
-- 여기에 적용할 SQL 작성
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);


-- migrate:down
-- 여기에 롤백 SQL 작성
DROP TABLE IF EXISTS users;
```

**두 부분 모두 필수:**
- `-- migrate:up`: 적용할 변경사항
- `-- migrate:down`: 롤백 방법

---

## 🔄 개발 워크플로우

### 새로운 컬럼 추가하기

**시나리오**: User에 `phone_number` 컬럼 추가

#### 1️⃣ 새 마이그레이션 생성

```bash
cd database

# 자동으로 타임스탬프가 붙은 파일 생성
dbmate new add_phone_number

# 결과: db/migrations/20250117123045_add_phone_number.sql
```

#### 2️⃣ SQL 작성

```sql
-- migrate:up
ALTER TABLE users
    ADD COLUMN phone_number VARCHAR(20);

COMMENT ON COLUMN users.phone_number IS '전화번호';


-- migrate:down
ALTER TABLE users
    DROP COLUMN phone_number;
```

#### 3️⃣ 마이그레이션 실행

```bash
# 적용
dbmate up

# 확인
dbmate status
```

#### 4️⃣ Entity 업데이트

```java
// User.java
@Entity
@Table(name = "users")
public class User {
    // ...

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // getter/setter 추가
}
```

#### 5️⃣ 애플리케이션 실행

```bash
cd ..
./gradlew bootRun

# JPA가 스키마 검증 → 성공!
```

---

## 🎭 주요 dbmate 명령어

```bash
# 마이그레이션 실행
dbmate up

# 마이그레이션 상태 확인
dbmate status

# 한 단계 롤백
dbmate down

# 전체 롤백
dbmate down --all

# 새 마이그레이션 생성
dbmate new <description>

# DB 초기화 (전체 재생성)
dbmate drop && dbmate up

# 현재 스키마 덤프
dbmate dump

# 도움말
dbmate --help
```

---

## 🆚 왜 Liquibase에서 dbmate로?

### ❌ 기존 방식 (Liquibase in Spring Boot)

```
./gradlew bootRun
    ↓
Spring Boot 시작
    ↓
Liquibase 자동 실행 (DB 변경)
    ↓
애플리케이션 시작
```

**문제점:**
- DB 변경이 애플리케이션 시작에 종속
- 프로덕션 배포 시 위험
- YAML/XML로 복잡한 설정
- DB 관리를 독립적으로 할 수 없음

### ✅ 새 방식 (dbmate 독립 실행)

```
# DB 관리 (독립!)
cd database
dbmate up

# 애플리케이션 실행 (분리!)
cd ..
./gradlew bootRun
```

**장점:**
- ✅ DB 변경을 독립적으로 관리
- ✅ 프로덕션 배포 전 DB 미리 마이그레이션
- ✅ 애플리케이션 재시작 없이 DB 변경 가능
- ✅ 순수 SQL로 명확한 제어
- ✅ 가볍고 빠름
- ✅ CI/CD 파이프라인에서 분리 실행

---

## 🎯 명세 기반 개발 흐름

### API 변경 시

```
1. OpenAPI 명세 수정 (api-spec.yaml)
   ↓
2. ./gradlew generateApi (코드 생성)
   ↓
3. Controller 구현 수정
```

### DB 변경 시 (독립적!)

```
1. dbmate new <description> (마이그레이션 생성)
   ↓
2. SQL 작성 (migrate:up / migrate:down)
   ↓
3. dbmate up (스키마 적용)
   ↓
4. Entity 수정 (User.java)
   ↓
5. ./gradlew bootRun (검증)
```

### 전체 흐름

```
API 명세 (OpenAPI)  ←→  DB 명세 (dbmate SQL)
       ↓                       ↓
   Controller  ←→  Service  ←→  Entity
       ↓                       ↓
   API 모델              Domain 모델
```

---

## 📊 Entity와 SQL 매핑

### Entity (User.java)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### SQL (20250101000001_create_users_table.sql)

```sql
-- migrate:up
CREATE TABLE users (
    -- @Id, @GeneratedValue(IDENTITY)
    id BIGSERIAL PRIMARY KEY,

    -- @Column(name = "name", nullable = false, length = 100)
    name VARCHAR(100) NOT NULL,

    -- @Column(name = "email", nullable = false, unique = true)
    email VARCHAR(255) NOT NULL UNIQUE,

    -- @Column(name = "age", nullable = false)
    age INTEGER NOT NULL,

    -- @Column(name = "created_at", updatable = false)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- @Column(name = "updated_at")
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 제약 조건 이름 명시
ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);

ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);


-- migrate:down
DROP TABLE IF EXISTS users;
```

---

## 🚨 주의사항

### 1. 마이그레이션 파일은 절대 수정하지 마세요

```bash
# ❌ 이미 적용된 파일 수정
vi db/migrations/20250101000001_create_users_table.sql

# ✅ 새로운 마이그레이션 추가
dbmate new fix_users_table
```

### 2. 파일 이름 규칙 준수

```
✅ 20250117123045_add_phone_number.sql
❌ add_phone_number.sql
❌ 001_add_phone_number.sql
```

### 3. migrate:up과 migrate:down 모두 작성

```sql
-- migrate:up
ALTER TABLE users ADD COLUMN status VARCHAR(20);

-- migrate:down
ALTER TABLE users DROP COLUMN status;
```

### 4. Spring Boot 설정 확인

```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # validate 또는 none만!
```

**절대 create, update, create-drop 사용 금지!**

---

## 🔧 유용한 명령어

### PostgreSQL 명령어

```bash
# DB 접속
docker exec -it springbasic-postgres psql -U springuser -d springbasic

# 테이블 목록
\dt

# 특정 테이블 구조
\d users

# 인덱스 목록
\di

# SQL 실행
SELECT * FROM users;

# 마이그레이션 이력
SELECT * FROM schema_migrations;

# 종료
\q
```

### Docker 명령어

```bash
# PostgreSQL 시작
docker-compose up -d

# 중지
docker-compose stop

# 완전 삭제 (데이터 포함)
docker-compose down -v

# 로그 보기
docker-compose logs -f postgres
```

---

## 🔗 Spring Boot 설정

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/springbasic
    username: springuser
    password: springpass

  jpa:
    hibernate:
      ddl-auto: validate  # 검증만! (스키마 생성 안 함)
    show-sql: true
```

**흐름:**
1. dbmate로 DB 스키마 생성/변경 (독립적!)
2. Spring Boot 시작
3. JPA가 스키마 검증 (validate)
4. 스키마 불일치 시 시작 실패 (안전!)

---

## 📖 예제: 전체 워크플로우

### 시나리오: User에 nickname 추가

```bash
# 1. 새 마이그레이션 생성
cd database
dbmate new add_nickname
```

```sql
-- 2. SQL 작성 (db/migrations/20250117_add_nickname.sql)
-- migrate:up
ALTER TABLE users
    ADD COLUMN nickname VARCHAR(50);

CREATE INDEX idx_users_nickname ON users(nickname);

COMMENT ON COLUMN users.nickname IS '닉네임';


-- migrate:down
DROP INDEX IF EXISTS idx_users_nickname;
ALTER TABLE users
    DROP COLUMN nickname;
```

```bash
# 3. 마이그레이션 실행
dbmate up

# 4. 확인
dbmate status
psql postgresql://springuser:springpass@localhost:5432/springbasic -c "\d users"
```

```java
// 5. Entity 업데이트 (src/main/java/.../User.java)
@Entity
@Table(name = "users")
public class User {
    // ...

    @Column(name = "nickname", length = 50)
    private String nickname;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
```

```bash
# 6. 애플리케이션 실행
cd ..
./gradlew bootRun

# JPA가 스키마 검증 → 성공!
```

---

## 🔗 관련 문서

- [database/README.md](../database/README.md) - DB 관리 상세 가이드
- [SPEC_FIRST_DEVELOPMENT.md](SPEC_FIRST_DEVELOPMENT.md) - API 명세 우선 개발
- [LAYER_SEPARATION.md](LAYER_SEPARATION.md) - 계층 분리

---

**이제 DB 관리가 애플리케이션과 독립적입니다!** 🎉
