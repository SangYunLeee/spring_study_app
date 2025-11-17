# Database Management (dbmate)

**DB 스키마는 애플리케이션과 독립적으로 관리됩니다!**

## 🎯 구조

```
database/
├── .env                    # DB 연결 설정
├── db/
│   ├── migrations/         # 마이그레이션 파일 (순수 SQL)
│   │   ├── 20250101000001_create_users_table.sql
│   │   ├── 20250101000002_add_email_index.sql
│   │   └── 20250101000003_add_updated_at_trigger.sql
│   └── schema.sql          # 현재 스키마 (dbmate가 자동 생성)
└── README.md
```

---

## 🚀 빠른 시작

### 1. dbmate 설치

```bash
# Mac
brew install dbmate

# Linux
curl -fsSL -o /usr/local/bin/dbmate https://github.com/amacneil/dbmate/releases/latest/download/dbmate-linux-amd64
chmod +x /usr/local/bin/dbmate

# Windows (Scoop)
scoop install dbmate
```

### 2. PostgreSQL 시작

```bash
# 프로젝트 루트에서
cd ..
docker-compose up -d

# 확인
docker-compose ps
```

### 3. 마이그레이션 실행

```bash
cd database

# 모든 마이그레이션 적용
dbmate up

# 상태 확인
dbmate status

# 한 단계 롤백
dbmate down

# 전체 재생성
dbmate drop && dbmate up
```

---

## 📝 새로운 마이그레이션 만들기

### 자동 생성 (타임스탬프 자동)

```bash
cd database

# 새 마이그레이션 파일 생성
dbmate new add_phone_number

# 결과: db/migrations/20250117123045_add_phone_number.sql
```

### 수동 생성

```bash
# 파일 이름 규칙: YYYYMMDDHHMMSS_description.sql
vi db/migrations/20250117123045_add_phone_number.sql
```

```sql
-- migrate:up
ALTER TABLE users
    ADD COLUMN phone_number VARCHAR(20);

COMMENT ON COLUMN users.phone_number IS '전화번호';


-- migrate:down
ALTER TABLE users
    DROP COLUMN phone_number;
```

### 적용

```bash
dbmate up
```

---

## 🔄 개발 워크플로우

### 1️⃣ DB 스키마 변경

```bash
# 1. 새 마이그레이션 생성
cd database
dbmate new add_user_status

# 2. SQL 작성
vi db/migrations/20250117_add_user_status.sql
```

```sql
-- migrate:up
ALTER TABLE users
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active';

CREATE INDEX idx_users_status ON users(status);


-- migrate:down
DROP INDEX IF EXISTS idx_users_status;
ALTER TABLE users
    DROP COLUMN status;
```

```bash
# 3. 마이그레이션 실행
dbmate up
```

### 2️⃣ Spring Boot Entity 업데이트

```java
// User.java
@Entity
@Table(name = "users")
public class User {
    // ...

    @Column(name = "status", length = 20, nullable = false)
    private String status = "active";

    // getter/setter
}
```

### 3️⃣ 애플리케이션 실행

```bash
cd ..
./gradlew bootRun

# JPA가 스키마 검증 (validate) → 성공!
```

---

## 🎭 주요 명령어

```bash
# 마이그레이션 실행
dbmate up

# 마이그레이션 상태 확인
dbmate status

# 한 단계 롤백
dbmate down

# 전체 롤백
dbmate down --all

# 전체 재생성 (개발 시)
dbmate drop && dbmate up

# 새 마이그레이션 생성
dbmate new <description>

# 현재 스키마 덤프
dbmate dump

# 도움말
dbmate --help
```

---

## 🆚 애플리케이션과 분리된 이유

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
- DB 관리를 독립적으로 할 수 없음

### ✅ 새 방식 (dbmate 독립 실행)

```
# DB 관리 (독립)
cd database
dbmate up

# 애플리케이션 실행 (분리)
cd ..
./gradlew bootRun
```

**장점:**
- ✅ DB 변경을 독립적으로 관리
- ✅ 프로덕션 배포 전 DB 미리 마이그레이션
- ✅ 애플리케이션 재시작 없이 DB 변경 가능
- ✅ 순수 SQL로 명확한 제어
- ✅ CI/CD 파이프라인에서 분리 실행

---

## 🔗 Spring Boot 설정

```yaml
# application.yml
spring:
  liquibase:
    enabled: false  # Liquibase 비활성화!

  jpa:
    hibernate:
      ddl-auto: validate  # 검증만! (스키마 생성 안 함)
```

**흐름:**
1. dbmate로 DB 스키마 생성/변경
2. Spring Boot는 스키마 검증만 수행
3. 스키마 불일치 시 시작 실패 (안전!)

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

---

## 📊 예제: 전체 워크플로우

### 시나리오: User에 nickname 추가

```bash
# 1. 새 마이그레이션 생성
cd database
dbmate new add_nickname

# 2. SQL 작성
vi db/migrations/20250117_add_nickname.sql
```

```sql
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
psql $DATABASE_URL -c "\d users"
```

```java
// 5. Entity 업데이트
@Entity
@Table(name = "users")
public class User {
    // ...

    @Column(name = "nickname", length = 50)
    private String nickname;
}
```

```bash
# 6. 애플리케이션 실행
cd ..
./gradlew bootRun
```

---

## 🔗 관련 링크

- [dbmate 공식 문서](https://github.com/amacneil/dbmate)
- [PostgreSQL 문서](https://www.postgresql.org/docs/)

---

**DB 관리가 이제 독립적입니다!** 🎉
