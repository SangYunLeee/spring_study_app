# Liquibase + Entity 매핑 검증 가이드

## 🔍 검증 방법 3가지

### 방법 1: 애플리케이션 시작 시 자동 검증 ⭐ 추천!

Spring Boot의 `ddl-auto: validate` 설정으로 자동 검증합니다.

#### 동작 원리

```yaml
# application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # 핵심 설정!
```

**실행 순서:**
```
1. Liquibase가 DB 스키마 생성
   ↓
2. JPA Hibernate가 Entity 읽기
   ↓
3. Entity와 실제 DB 스키마 비교
   ↓
4. 불일치 발견 시 → 에러 발생 + 시작 실패 ❌
5. 일치하면 → 정상 시작 ✅
```

#### 테스트 방법

**1단계: PostgreSQL 시작**
```bash
docker-compose up -d
```

**2단계: 애플리케이션 실행**
```bash
./gradlew bootRun
```

**3단계: 로그 확인**

✅ **성공 시:**
```
Liquibase: Successfully released change log lock
Hibernate:
    select
        constraint_name,
        table_name,
        column_name
    from
        information_schema.key_column_usage
...
Started SpringBasicApplication in 3.456 seconds
```

❌ **실패 시 (매핑 불일치):**
```
Schema-validation: missing column [phone_number] in table [users]
```

---

### 방법 2: 수동으로 스키마 비교

#### DB 스키마 확인

```bash
# PostgreSQL 접속
docker exec -it springbasic-postgres psql -U springuser -d springbasic

# 테이블 구조 확인
\d users
```

**출력 예시:**
```
                                        Table "public.users"
   Column   |            Type             | Collation | Nullable |              Default
------------+-----------------------------+-----------+----------+-----------------------------------
 id         | bigint                      |           | not null | nextval('users_id_seq'::regclass)
 name       | character varying(100)      |           | not null |
 email      | character varying(255)      |           | not null |
 age        | integer                     |           | not null |
 created_at | timestamp without time zone |           | not null | CURRENT_TIMESTAMP
 updated_at | timestamp without time zone |           | not null | CURRENT_TIMESTAMP
Indexes:
    "pk_users" PRIMARY KEY, btree (id)
    "uk_users_email" UNIQUE CONSTRAINT, btree (email)
    "idx_users_email" btree (email)
```

#### Entity 확인

```java
// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                           // → id BIGINT

    @Column(name = "name", nullable = false, length = 100)
    private String name;                       // → name VARCHAR(100)

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;                      // → email VARCHAR(255) UNIQUE

    @Column(name = "age", nullable = false)
    private Integer age;                       // → age INTEGER

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;           // → created_at TIMESTAMP

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;           // → updated_at TIMESTAMP
}
```

#### 매핑 테이블

| Entity | 타입 | DB | 타입 | 매칭 |
|--------|------|----|------|------|
| `id` | Long | `id` | BIGSERIAL | ✅ |
| `name` | String | `name` | VARCHAR(100) | ✅ |
| `email` | String | `email` | VARCHAR(255) UNIQUE | ✅ |
| `age` | Integer | `age` | INTEGER | ✅ |
| `createdAt` | LocalDateTime | `created_at` | TIMESTAMP | ✅ |
| `updatedAt` | LocalDateTime | `updated_at` | TIMESTAMP | ✅ |

---

### 방법 3: 직접 데이터 저장 테스트

실제로 데이터를 저장해서 확인하는 가장 확실한 방법!

#### 테스트 시나리오

**1단계: API로 사용자 생성**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "테스트유저",
    "email": "test@example.com",
    "age": 30
  }'
```

**2단계: DB에서 확인**
```bash
docker exec -it springbasic-postgres psql -U springuser -d springbasic

SELECT * FROM users;
```

**출력 예시:**
```
 id |   name      |        email        | age |       created_at        |       updated_at
----+-------------+---------------------+-----+-------------------------+-------------------------
  1 | 테스트유저   | test@example.com    |  30 | 2025-11-06 10:30:45.123 | 2025-11-06 10:30:45.123
```

**3단계: API로 조회**
```bash
curl http://localhost:8080/api/users/1
```

**출력:**
```json
{
  "id": 1,
  "name": "테스트유저",
  "email": "test@example.com",
  "age": 30
}
```

✅ **DB에 저장된 데이터와 API 응답이 일치하면 매핑 성공!**

---

## 🚨 불일치 발견 시 해결 방법

### 문제 1: 컬럼명 불일치

**에러:**
```
Schema-validation: missing column [phone_number] in table [users]
```

**원인:**
- Entity에는 `phoneNumber` 필드가 있는데
- DB에는 `phone_number` 컬럼이 없음

**해결:**
```yaml
# 새 changeset 작성
# db/changelog/changes/004-add-phone-number.yaml
databaseChangeLog:
  - changeSet:
      id: 004-add-phone-number
      author: developer
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: phone_number
                  type: VARCHAR(20)
```

### 문제 2: 타입 불일치

**에러:**
```
Wrong column type in table 'users' for column 'age'.
Found: varchar, expected: integer
```

**원인:**
- Entity: `Integer age`
- DB: `VARCHAR age`

**해결:**
```yaml
# Liquibase로 타입 변경
- modifyDataType:
    tableName: users
    columnName: age
    newDataType: INTEGER
```

### 문제 3: Nullable 불일치

**에러:**
```
Schema-validation: column [email] is nullable, should be not null
```

**원인:**
- Entity: `nullable = false`
- DB: nullable 제약 없음

**해결:**
```yaml
- addNotNullConstraint:
    tableName: users
    columnName: email
    columnDataType: VARCHAR(255)
```

---

## ✅ 완전한 검증 체크리스트

### 1. 애플리케이션 시작 확인
```bash
./gradlew bootRun
```
- [ ] 에러 없이 시작됨
- [ ] "Started SpringBasicApplication" 로그 확인

### 2. 데이터 저장 테스트
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@test.com","age":25}'
```
- [ ] 201 Created 응답
- [ ] id가 할당됨

### 3. DB 직접 확인
```bash
docker exec -it springbasic-postgres psql -U springuser -d springbasic
SELECT * FROM users;
```
- [ ] 데이터가 저장됨
- [ ] created_at, updated_at 자동 설정됨

### 4. 조회 테스트
```bash
curl http://localhost:8080/api/users/1
```
- [ ] 저장한 데이터와 일치

### 5. 수정 테스트
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated","email":"test@test.com","age":26}'
```
- [ ] updated_at이 자동으로 변경됨

---

## 🔧 고급 검증: SQL 로그 확인

### 설정

```yaml
# application.yml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 실행 시 로그

**사용자 저장 시:**
```sql
Hibernate:
    insert
    into
        users
        (age, created_at, email, name, updated_at)
    values
        (?, ?, ?, ?, ?)
```

✅ **컬럼명이 Entity와 일치하는지 확인!**

**사용자 조회 시:**
```sql
Hibernate:
    select
        u1_0.id,
        u1_0.age,
        u1_0.created_at,
        u1_0.email,
        u1_0.name,
        u1_0.updated_at
    from
        users u1_0
    where
        u1_0.id=?
```

✅ **모든 컬럼이 조회되는지 확인!**

---

## 📊 매핑 확인 스크립트

실전에서 사용할 수 있는 검증 스크립트:

```bash
#!/bin/bash
# verify-mapping.sh

echo "=== DB 스키마 확인 ==="
docker exec -it springbasic-postgres psql -U springuser -d springbasic -c "\d users"

echo ""
echo "=== 테스트 데이터 삽입 ==="
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"MappingTest","email":"mapping@test.com","age":99}' \
  -s | jq

echo ""
echo "=== DB에서 확인 ==="
docker exec -it springbasic-postgres psql -U springuser -d springbasic \
  -c "SELECT * FROM users WHERE email='mapping@test.com';"

echo ""
echo "=== API로 확인 ==="
USER_ID=$(curl -s http://localhost:8080/api/users | jq '.[] | select(.email=="mapping@test.com") | .id')
curl -s http://localhost:8080/api/users/$USER_ID | jq

echo ""
echo "✅ 매핑 검증 완료!"
```

---

## 🎯 요약

### 가장 쉬운 방법
```bash
# 1. PostgreSQL 시작
docker-compose up -d

# 2. 애플리케이션 실행
./gradlew bootRun

# 3. 에러 없이 시작되면 → 매핑 성공! ✅
```

### 가장 확실한 방법
```bash
# 1. 데이터 저장
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@test.com","age":25}'

# 2. DB 확인
docker exec -it springbasic-postgres psql -U springuser -d springbasic
SELECT * FROM users;

# 3. 데이터 일치 → 매핑 성공! ✅
```

---

**핵심: `ddl-auto: validate` 설정으로 자동 검증됩니다!** ✅