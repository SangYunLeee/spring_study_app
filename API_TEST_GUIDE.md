# RESTful API 테스트 가이드

이 문서는 구현한 사용자 관리 RESTful API의 모든 엔드포인트 테스트 방법을 제공합니다.

## 🚀 시작하기

애플리케이션이 실행 중인지 확인:
```bash
./gradlew bootRun
```

서버: http://localhost:8080

---

## 📝 전체 API 목록

| 메서드 | URI | 설명 | 상태 코드 |
|--------|-----|------|-----------|
| POST | `/api/users` | 사용자 생성 | 201, 400 |
| GET | `/api/users` | 전체 사용자 조회 | 200 |
| GET | `/api/users/{id}` | 사용자 조회 | 200, 404 |
| GET | `/api/users/search?keyword=xxx` | 이름으로 검색 | 200 |
| GET | `/api/users/adults` | 성인만 조회 | 200 |
| GET | `/api/users/statistics` | 통계 조회 | 200 |
| PUT | `/api/users/{id}` | 전체 수정 | 200, 400, 404 |
| PATCH | `/api/users/{id}` | 부분 수정 | 200, 400, 404 |
| DELETE | `/api/users/{id}` | 사용자 삭제 | 204, 404 |

---

## 1️⃣ POST - 사용자 생성

### 요청

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "email": "hong@example.com",
    "age": 25
  }'
```

### 성공 응답 (201 Created)

```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "age": 25
}
```

### HTTP 헤더
- `Location: /api/users/1` - 생성된 리소스 URI

### 실패 케이스

**중복 이메일** (400 Bad Request)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "다른사람",
    "email": "hong@example.com",
    "age": 30
  }'
```

**잘못된 데이터** (400 Bad Request)
```bash
# 이메일 형식 오류
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "김철수",
    "email": "invalid-email",
    "age": 25
  }'
```

---

## 2️⃣ GET - 조회

### 2-1. 전체 사용자 조회

```bash
curl http://localhost:8080/api/users
```

**응답 (200 OK)**
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "age": 25
  },
  {
    "id": 2,
    "name": "김철수",
    "email": "kim@example.com",
    "age": 30
  }
]
```

### 2-2. ID로 사용자 조회

```bash
curl http://localhost:8080/api/users/1
```

**성공 (200 OK)**
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "age": 25
}
```

**실패 (404 Not Found)**
```bash
curl http://localhost:8080/api/users/999
```

### 2-3. 이름으로 검색

```bash
curl "http://localhost:8080/api/users/search?keyword=홍"
```

**응답**
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "age": 25
  }
]
```

### 2-4. 성인만 조회

```bash
curl http://localhost:8080/api/users/adults
```

**응답** (나이 >= 19인 사용자만)
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "age": 25
  },
  {
    "id": 2,
    "name": "김철수",
    "email": "kim@example.com",
    "age": 30
  }
]
```

### 2-5. 통계 조회

```bash
curl http://localhost:8080/api/users/statistics
```

**응답**
```json
{
  "totalCount": 3,
  "adultCount": 2,
  "averageAge": 24.0,
  "minAge": 17,
  "maxAge": 30
}
```

---

## 3️⃣ PUT - 전체 수정

**모든 필드를 제공해야 합니다**

### 요청

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동_수정",
    "email": "hong_new@example.com",
    "age": 26
  }'
```

### 성공 응답 (200 OK)

```json
{
  "id": 1,
  "name": "홍길동_수정",
  "email": "hong_new@example.com",
  "age": 26
}
```

### 실패 케이스

**사용자 없음** (404 Not Found)
```bash
curl -X PUT http://localhost:8080/api/users/999 \
  -H "Content-Type: application/json" \
  -d '{"name":"테스트","email":"test@example.com","age":25}'
```

**이메일 중복** (400 Bad Request)
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "email": "kim@example.com",
    "age": 25
  }'
```

---

## 4️⃣ PATCH - 부분 수정

**변경할 필드만 제공하면 됩니다**

### 4-1. 나이만 수정

```bash
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "age": 27
  }'
```

**응답** (200 OK)
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "age": 27
}
```

### 4-2. 이름과 이메일만 수정

```bash
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동_새이름",
    "email": "hong_updated@example.com"
  }'
```

**응답**
```json
{
  "id": 1,
  "name": "홍길동_새이름",
  "email": "hong_updated@example.com",
  "age": 27
}
```

### 4-3. 빈 요청 (실패)

```bash
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{}'
```

**응답** (400 Bad Request)

---

## 5️⃣ DELETE - 삭제

### 요청

```bash
curl -X DELETE http://localhost:8080/api/users/1
```

### 성공 응답 (204 No Content)

응답 본문 없음

### 헤더 확인

```bash
curl -X DELETE http://localhost:8080/api/users/1 -v 2>&1 | grep "< HTTP"
```

**출력**
```
< HTTP/1.1 204
```

### 실패 케이스 (404 Not Found)

```bash
curl -X DELETE http://localhost:8080/api/users/999 -v 2>&1 | grep "< HTTP"
```

**출력**
```
< HTTP/1.1 404
```

---

## 🎯 실전 테스트 시나리오

### 시나리오 1: 사용자 생성 → 조회 → 수정 → 삭제

```bash
# 1. 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"테스트유저","email":"test@example.com","age":25}'

# 2. 생성된 사용자 조회 (ID=1이라고 가정)
curl http://localhost:8080/api/users/1

# 3. 나이만 수정 (PATCH)
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"age":26}'

# 4. 전체 정보 수정 (PUT)
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"수정된이름","email":"updated@example.com","age":30}'

# 5. 삭제
curl -X DELETE http://localhost:8080/api/users/1

# 6. 삭제 확인 (404 반환해야 함)
curl http://localhost:8080/api/users/1
```

### 시나리오 2: 여러 사용자 생성 후 검색

```bash
# 1. 여러 사용자 생성
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com","age":25}'

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길순","email":"hong2@example.com","age":23}'

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"김철수","email":"kim@example.com","age":30}'

curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"이영희","email":"lee@example.com","age":17}'

# 2. 전체 조회
curl http://localhost:8080/api/users

# 3. "홍"으로 검색 (2명 나와야 함)
curl "http://localhost:8080/api/users/search?keyword=홍"

# 4. 성인만 ���회 (3명 나와야 함)
curl http://localhost:8080/api/users/adults

# 5. 통계 조회
curl http://localhost:8080/api/users/statistics
```

---

## 🔍 PUT vs PATCH 차이 비교

### 현재 데이터
```json
{
  "id": 1,
  "name": "홍길동",
  "email": "hong@example.com",
  "age": 25
}
```

### PUT으로 나이만 변경하려면? (❌ 실패)
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"age":26}'
```
→ 400 Bad Request (name, email 필드 누락)

### PUT은 모든 필드 필요 (✅ 성공)
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "email": "hong@example.com",
    "age": 26
  }'
```

### PATCH는 변경할 필드만 (✅ 성공)
```bash
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"age":26}'
```

---

## 💡 주요 HTTP 상태 코드

| 코드 | 의미 | 사용 예 |
|------|------|---------|
| 200 | OK | 조회, 수정 성공 |
| 201 | Created | 생성 성공 |
| 204 | No Content | 삭제 성공 (본문 없음) |
| 400 | Bad Request | 유효성 검증 실패 |
| 404 | Not Found | 리소스 없음 |

---

## 🛠 유용한 curl 옵션

```bash
# 응답 헤더 포함
curl -i http://localhost:8080/api/users/1

# 상세 정보 출력
curl -v http://localhost:8080/api/users/1

# 보기 좋게 정렬 (jq 필요)
curl http://localhost:8080/api/users | jq

# HTTP 상태 코드만 확인
curl -X DELETE http://localhost:8080/api/users/1 -w "%{http_code}\n" -o /dev/null -s
```

---

## 📚 관련 문서

- [RESTFUL_API.md](RESTFUL_API.md) - RESTful API 이론
- [UserController.java](src/main/java/com/example/springbasic/controller/UserController.java) - 실제 구현 코드
- [LAYER_ARCHITECTURE.md](LAYER_ARCHITECTURE.md) - 계층 구조 설명

---

## ✅ 전체 테스트 완료 확인 목록

- [ ] POST로 사용자 생성 (201)
- [ ] GET으로 전체 조회 (200)
- [ ] GET으로 ID 조회 (200, 404)
- [ ] GET으로 이름 검색
- [ ] PUT으로 전체 수정 (200)
- [ ] PATCH로 부분 수정 (200)
- [ ] DELETE로 삭제 (204)
- [ ] 중복 이메일 생성 시도 (400)
- [ ] 잘못된 데이터로 생성 시도 (400)
- [ ] 없는 사용자 조회 (404)

모든 테스트를 통과했다면 RESTful API 완성입니다! 🎉