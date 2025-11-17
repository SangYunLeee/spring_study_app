# 빠른 시작 가이드

## ⚡ 5분 안에 시작하기

### 1. 도구 설치 (1회만)

```bash
# DBML CLI
npm install -g @dbml/cli

# dbmate
brew install dbmate  # Mac
# 또는 database/README.md 참조
```

### 2. DB 명세 작성

```bash
# database/schema.dbml 수정
vi schema.dbml
```

### 3. 시각화 (선택)

1. https://dbdiagram.io/d 접속
2. `schema.dbml` 내용 붙여넣기
3. ERD 확인!

### 4. SQL 생성 → 마이그레이션

```bash
# SQL 생성
./scripts/dbml-to-sql.sh

# 마이그레이션 생성
dbmate new my_change

# generated/schema.sql에서 필요한 부분 복사
# → db/migrations/xxx_my_change.sql

# 적용
dbmate up
```

---

## 📚 상세 가이드

- [database/README.md](README.md) - 전체 가이드
- [database/DBML_WORKFLOW.md](DBML_WORKFLOW.md) - DBML 상세 설명

---

## 🎯 핵심 개념

```
schema.dbml → dbdiagram.io → SQL → dbmate → PostgreSQL
   (명세)      (시각화)      (생성)  (실행)
```

**장점:**
- ✅ ERD와 스키마 항상 일치
- ✅ Git으로 명세 버전 관리
- ✅ 자동 SQL 생성
