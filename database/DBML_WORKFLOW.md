# DBML 기반 DB 명세 워크플로우

**dbdiagram.io를 사용한 시각적 DB 설계!**

## 🎨 왜 DBML인가?

### 기존 방식의 문제

```
SQL 파일 → 머릿속 ERD → 다시 SQL 수정 → 문서화?
```

**문제점:**
- ❌ ERD와 실제 SQL이 불일치
- ❌ 시각화 어려움
- ❌ 변경 사항 추적 어려움

### DBML 방식

```
schema.dbml (명세) → dbdiagram.io (시각화) → SQL 생성 → 마이그레이션
```

**장점:**
- ✅ ERD와 스키마가 항상 일치
- ✅ 브라우저에서 즉시 시각화
- ✅ Git으로 명세 버전 관리
- ✅ 자동으로 SQL 생성

---

## 🚀 전체 워크플로우

### 1️⃣ schema.dbml 작성 (명세 작성)

```dbml
// database/schema.dbml
Table users {
  id bigserial [pk, increment]
  name varchar(100) [not null]
  email varchar(255) [not null, unique]
  age integer [not null]
  created_at timestamp [not null, default: `CURRENT_TIMESTAMP`]

  indexes {
    email [name: 'idx_users_email']
  }

  Note: '사용자 테이블'
}

Table posts {
  id bigserial [pk, increment]
  user_id bigint [not null, ref: > users.id]  // Foreign Key!
  title varchar(255) [not null]
  content text
  created_at timestamp [not null, default: `CURRENT_TIMESTAMP`]
}
```

### 2️⃣ dbdiagram.io에서 시각화

```bash
# schema.dbml 파일 복사
cat database/schema.dbml | pbcopy  # Mac
# 또는 직접 파일 열기
```

1. https://dbdiagram.io/d 접속
2. DBML 붙여넣기
3. 실시간 ERD 확인!

**기능:**
- 테이블 관계 자동 표시
- 드래그로 레이아웃 조정
- PNG/PDF 내보내기
- 공유 링크 생성

### 3️⃣ SQL 생성

```bash
cd database

# DBML → PostgreSQL SQL 변환
./scripts/dbml-to-sql.sh

# 결과: generated/schema.sql
```

### 4️⃣ 마이그레이션 생성

```bash
# 새 마이그레이션 파일 생성
dbmate new add_posts_table

# generated/schema.sql에서 필요한 부분 복사
# → db/migrations/xxx_add_posts_table.sql
```

```sql
-- migrate:up
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_posts_user_id ON posts(user_id);


-- migrate:down
DROP TABLE IF EXISTS posts;
```

### 5️⃣ 적용

```bash
dbmate up
```

---

## 📖 실전 예제: 새 테이블 추가

### 시나리오: 게시글 댓글 기능 추가

#### 1. schema.dbml 수정

```dbml
Table comments {
  id bigserial [pk, increment, note: '댓글 ID']
  post_id bigint [not null, ref: > posts.id, note: '게시글 ID']
  user_id bigint [not null, ref: > users.id, note: '작성자 ID']
  content text [not null, note: '댓글 내용']
  created_at timestamp [not null, default: `CURRENT_TIMESTAMP`]
  updated_at timestamp [not null, default: `CURRENT_TIMESTAMP`]

  indexes {
    post_id [name: 'idx_comments_post_id']
    user_id [name: 'idx_comments_user_id']
    (post_id, created_at) [name: 'idx_comments_post_created']
  }

  Note: '''
    게시글 댓글 테이블

    비즈니스 규칙:
    - 게시글 삭제 시 댓글도 함께 삭제
    - 사용자는 자신의 댓글만 수정/삭제 가능
  '''
}
```

#### 2. dbdiagram.io에서 확인

```
Users (1) ←─── (N) Comments
Posts (1) ←─── (N) Comments
```

**확인 사항:**
- [ ] 외래 키 관계가 올바른가?
- [ ] 인덱스가 필요한 컬럼에 있는가?
- [ ] 테이블 구조가 정규화되어 있는가?

#### 3. SQL 생성 및 마이그레이션

```bash
# SQL 생성
./scripts/dbml-to-sql.sh

# 마이그레이션 생성
dbmate new add_comments_table

# generated/schema.sql에서 comments 테이블 부분 복사
vi db/migrations/20250117_add_comments_table.sql
```

```sql
-- migrate:up
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comments_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
        REFERENCES users(id)
);

CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_post_created ON comments(post_id, created_at);

COMMENT ON TABLE comments IS '게시글 댓글 테이블';
COMMENT ON COLUMN comments.id IS '댓글 ID';
COMMENT ON COLUMN comments.post_id IS '게시글 ID';
COMMENT ON COLUMN comments.user_id IS '작성자 ID';
COMMENT ON COLUMN comments.content IS '댓글 내용';


-- migrate:down
DROP TABLE IF EXISTS comments;
```

```bash
# 적용
dbmate up
```

---

## 🎯 DBML 문법 치트시트

### 기본 문법

```dbml
// 테이블 정의
Table 테이블명 {
  컬럼명 타입 [속성]

  indexes {
    컬럼명 [name: '인덱스명']
  }

  Note: '설명'
}

// 관계 정의
Ref: table1.column > table2.column  // Many-to-One
Ref: table1.column < table2.column  // One-to-Many
Ref: table1.column - table2.column  // One-to-One
Ref: table1.column <> table2.column // Many-to-Many
```

### 데이터 타입 (PostgreSQL)

```dbml
id bigserial              // BIGINT AUTO_INCREMENT
name varchar(100)         // VARCHAR(100)
age integer               // INTEGER
price decimal(10,2)       // DECIMAL(10,2)
content text              // TEXT
is_active boolean         // BOOLEAN
created_at timestamp      // TIMESTAMP
data jsonb                // JSONB (PostgreSQL)
```

### 컬럼 속성

```dbml
id bigserial [pk]                           // Primary Key
email varchar(255) [unique]                 // Unique
age integer [not null]                      // Not Null
status varchar(20) [default: 'active']      // Default
price decimal [note: '가격']                // 주석
created_at timestamp [default: `CURRENT_TIMESTAMP`]
```

### 인덱스

```dbml
indexes {
  email [name: 'idx_users_email']                    // 단일 컬럼
  (user_id, created_at) [name: 'idx_posts_user']     // 복합 인덱스
  email [unique, name: 'uk_users_email']             // Unique 인덱스
}
```

### 외래 키

```dbml
// 인라인 방식
Table posts {
  user_id bigint [ref: > users.id]
}

// 별도 선언 방식
Ref: posts.user_id > users.id

// 삭제 옵션
Ref: comments.post_id > posts.id [delete: cascade]
Ref: orders.user_id > users.id [delete: set null]
```

---

## 🔗 유용한 링크

- [dbdiagram.io](https://dbdiagram.io/) - DBML 시각화 도구
- [DBML 문법 가이드](https://dbml.dbdiagram.io/docs/)
- [DBML CLI GitHub](https://github.com/holistics/dbml)

---

## 💡 팁

### 1. schema.dbml을 항상 최신으로 유지

```bash
# 매번 마이그레이션 후
vi schema.dbml  # 변경사항 반영
```

### 2. dbdiagram.io에서 공유

```
1. ERD 작성
2. Export → Share Link
3. 팀원에게 공유
```

### 3. 프로젝트 설정으로 명세 관리

```dbml
Project springbasic {
  database_type: 'PostgreSQL'
  Note: '''
    Spring Basic Application

    명세 우선 개발:
    - API: OpenAPI
    - DB: DBML
  '''
}
```

---

**이제 DB 설계가 시각적이고 명확합니다!** 🎨
