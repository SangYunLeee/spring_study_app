# DB 명세 관리 (Liquibase + PostgreSQL)

API는 OpenAPI로, DB는 Liquibase로 명세를 관리합니다!

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
                      ↓
┌─────────────────────────────────────────────────────────┐
│        DB 명세 (Liquibase)                               │
│        src/main/resources/db/changelog/                  │
│        - 001-create-users-table.yaml                     │
│        - 002-add-email-index.yaml                        │
│        - 003-add-timestamps.yaml                         │
└─────────────────────────────────────────────────────────┘
                      ↓ 스키마 적용
┌─────────────────────────────────────────────────────────┐
│        PostgreSQL Database                               │
│        docker-compose로 실행                             │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 빠른 시작

### 1. PostgreSQL 시작 (Docker)

```bash
# Docker Compose로 PostgreSQL 시작
docker-compose up -d

# 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs postgres
```

### 2. 애플리케이션 실행

```bash
# Liquibase가 자동으로 스키마 생성
./gradlew bootRun
```

**실행 순서:**
1. Spring Boot 시작
2. Liquibase가 `db.changelog-master.yaml` 읽기
3. 변경사항 확인 (databasechangelog 테이블)
4. 새로운 changeset 실행
5. JPA가 스키마 검증 (validate)
6. 애플리케이션 시작 완료

### 3. DB 확인

```bash
# PostgreSQL 접속
docker exec -it springbasic-postgres psql -U springuser -d springbasic

# 테이블 목록
\dt

# users 테이블 구조
\d users

# 데이터 조회
SELECT * FROM users;

# Liquibase 이력
SELECT * FROM databasechangelog;

# 종료
\q
```

---

## 📁 디렉토리 구조

```
src/main/resources/
├── db/
│   └── changelog/
│       ├── db.changelog-master.yaml        # 메인 파일
│       └── changes/
│           ├── 001-create-users-table.yaml # 테이블 생성
│           ├── 002-add-email-index.yaml    # 인덱스 추가
│           └── 003-add-timestamps.yaml     # 타임스탬프 추가
│
├── application.yml                         # 기본 설정
└── application-dev.yml                     # 개발 환경 설정
```

---

## 📝 Liquibase 명세 파일 구조

### 메인 파일 (db.changelog-master.yaml)

```yaml
databaseChangeLog:
  # 변경사항을 순서대로 나열
  - include:
      file: db/changelog/changes/001-create-users-table.yaml

  - include:
      file: db/changelog/changes/002-add-email-index.yaml

  - include:
      file: db/changelog/changes/003-add-timestamps.yaml
```

### 변경사항 파일 (001-create-users-table.yaml)

```yaml
databaseChangeLog:
  - changeSet:
      id: 001-create-users-table  # 고유 ID
      author: developer             # 작성자
      comment: 사용자 테이블 생성   # 설명
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: BIGSERIAL
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
                  remarks: 사용자 고유 ID

      rollback:  # 롤백 방법
        - dropTable:
            tableName: users
```

---

## 🔄 개발 워크플로우

### 새로운 컬럼 추가하기

**시나리오**: User에 `phone_number` 컬럼 추가

#### 1️⃣ Liquibase 명세 작성

```bash
# 새 파일 생성
vi src/main/resources/db/changelog/changes/004-add-phone-number.yaml
```

```yaml
databaseChangeLog:
  - changeSet:
      id: 004-add-phone-number
      author: developer
      comment: 전화번호 컬럼 추가
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: phone_number
                  type: VARCHAR(20)
                  constraints:
                    nullable: true
                  remarks: 전화번호

      rollback:
        - dropColumn:
            tableName: users
            columnName: phone_number
```

#### 2️⃣ 메인 파일에 추가

```yaml
# db.changelog-master.yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/001-create-users-table.yaml
  - include:
      file: db/changelog/changes/002-add-email-index.yaml
  - include:
      file: db/changelog/changes/003-add-timestamps.yaml
  - include:
      file: db/changelog/changes/004-add-phone-number.yaml  # 새로 추가!
```

#### 3️⃣ Entity 업데이트

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

#### 4️⃣ 실행 및 확인

```bash
# 애플리케이션 재시작 (자동으로 changeset 실행)
./gradlew bootRun

# 확인
docker exec -it springbasic-postgres psql -U springuser -d springbasic
\d users
```

---

## 🎭 주요 Liquibase 작업

### 테이블 생성

```yaml
- createTable:
    tableName: products
    columns:
      - column:
          name: id
          type: BIGSERIAL
          constraints:
            primaryKey: true
```

### 컬럼 추가

```yaml
- addColumn:
    tableName: users
    columns:
      - column:
          name: nickname
          type: VARCHAR(50)
```

### 컬럼 수정

```yaml
- modifyDataType:
    tableName: users
    columnName: email
    newDataType: VARCHAR(500)
```

### 인덱스 추가

```yaml
- createIndex:
    indexName: idx_users_name
    tableName: users
    columns:
      - column:
          name: name
```

### 외래 키 추가

```yaml
- addForeignKeyConstraint:
    baseTableName: orders
    baseColumnNames: user_id
    referencedTableName: users
    referencedColumnNames: id
    constraintName: fk_orders_user
```

### 데이터 삽입

```yaml
- insert:
    tableName: users
    columns:
      - column:
          name: name
          value: Admin
      - column:
          name: email
          value: admin@example.com
      - column:
          name: age
          value: 30
```

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

## 🆚 Hibernate DDL vs Liquibase

### Hibernate DDL (권장하지 않음)

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 또는 create, create-drop
```

**문제점:**
- ❌ 변경 이력 없음
- ❌ 롤백 불가
- ❌ 팀 협업 어려움
- ❌ 프로덕션 위험

### Liquibase (권장) ✅

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 검증만!
  liquibase:
    enabled: true
```

**장점:**
- ✅ 모든 변경 이력 추적
- ✅ 롤백 가능
- ✅ Git으로 협업
- ✅ 안전한 프로덕션 배포

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

### DB 변경 시

```
1. Liquibase 명세 작성 (004-xxx.yaml)
   ↓
2. Entity 수정 (User.java)
   ↓
3. ./gradlew bootRun (자동 적용)
```

### 전체 흐름

```
API 명세 (OpenAPI)  ←→  DB 명세 (Liquibase)
       ↓                       ↓
   Controller  ←→  Service  ←→  Entity
       ↓                       ↓
   API 모델              Domain 모델
```

---

## 📊 Entity와 Liquibase 매핑

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

### Liquibase (001-create-users-table.yaml)

```yaml
- createTable:
    tableName: users  # @Table(name = "users")
    columns:
      - column:
          name: id  # @Column(name = "id")
          type: BIGSERIAL  # @GeneratedValue(IDENTITY)
          autoIncrement: true
          constraints:
            primaryKey: true  # @Id
            nullable: false

      - column:
          name: name  # @Column(name = "name")
          type: VARCHAR(100)  # length = 100
          constraints:
            nullable: false  # nullable = false

      - column:
          name: email
          type: VARCHAR(255)
          constraints:
            nullable: false
            unique: true  # unique = true

      - column:
          name: age
          type: INTEGER
          constraints:
            nullable: false
```

---

## 🚨 주의사항

### 1. changeset ID는 변경하지 마세요

```yaml
- changeSet:
    id: 001-create-users-table  # 이미 실행되면 절대 변경 금지!
```

### 2. 실행된 changeset은 수정하지 마세요

- ❌ 이미 적용된 파일 수정
- ✅ 새로운 changeset 추가

### 3. ddl-auto는 validate만

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # validate 또는 none만!
```

---

## 🔗 관련 문서

- [SPEC_FIRST_DEVELOPMENT.md](SPEC_FIRST_DEVELOPMENT.md) - API 명세 우선 개발
- [LAYER_SEPARATION.md](LAYER_SEPARATION.md) - 계층 분리
- [WHY_NOT_API_MODEL_IN_SERVICE.md](WHY_NOT_API_MODEL_IN_SERVICE.md) - Service에서 API 모델 사용하지 않는 이유

---

**이제 API와 DB 모두 명세로 관리합니다!** 🎉