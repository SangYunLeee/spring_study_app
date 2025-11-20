# Spring Security + JWT 인증 구현 가이드

이 문서는 Spring Security와 JWT를 사용한 인증 시스템 구현 과정과 테스트 방법을 설명합니다.

## 목차
1. [구현 완료 사항](#구현-완료-사항)
2. [아키텍처 개요](#아키텍처-개요)
3. [주요 컴포넌트 설명](#주요-컴포넌트-설명)
4. [테스트 방법](#테스트-방법)
5. [트러블슈팅](#트러블슈팅)

---

## 구현 완료 사항

### ✅ 1. 데이터베이스 마이그레이션
- **파일**: `database/db/migrations/20250101000006_add_user_auth_fields.sql`
- **변경사항**:
  - `password` 컬럼 추가 (VARCHAR 255, BCrypt 암호화)
  - `role` 컬럼 추가 (VARCHAR 20, USER/ADMIN)
  - `idx_users_role` 인덱스 추가

### ✅ 2. 도메인 모델
- **Role.java**: 사용자 권한 Enum (USER, ADMIN)
- **User.java**: UserDetails 인터페이스 구현
  - Spring Security 통합 메서드 추가
  - UpdateRequest에 password, role 필드 추가

### ✅ 3. JWT 유틸리티
- **파일**: `src/main/java/com/example/springbasic/security/JwtUtil.java`
- **기능**:
  - 토큰 생성: `generateToken(UserDetails)`, `generateToken(String username)`
  - 토큰 검증: `validateToken(String token, UserDetails)`
  - Claims 추출: `extractUsername()`, `extractExpiration()`
- **설정**:
  - Secret Key: application.yml의 `jwt.secret`
  - 만료 시간: 24시간 (86400000ms)

### ✅ 4. 인증 서비스
- **파일**: `src/main/java/com/example/springbasic/service/AuthService.java`
- **메서드**:
  - `register(RegisterRequest)`: 회원가입
    1. 이메일 중복 체크
    2. 비밀번호 BCrypt 암호화
    3. User 생성 (기본 권한: USER)
    4. JWT 토큰 발급
  - `login(LoginRequest)`: 로그인
    1. 이메일로 사용자 조회
    2. 비밀번호 검증
    3. JWT 토큰 발급
  - `loadUserByUsername(String email)`: Spring Security 통합

### ✅ 5. Spring Security 설정
- **파일**: `src/main/java/com/example/springbasic/config/SecurityConfig.java`
- **설정**:
  - CSRF 비활성화 (REST API)
  - Session STATELESS (JWT 방식)
  - 공개 API: `/api/auth/**`, Swagger UI
  - 보호된 API: 나머지 모든 요청
  - BCryptPasswordEncoder Bean 등록

### ✅ 6. JWT 인증 필터
- **파일**: `src/main/java/com/example/springbasic/security/JwtAuthenticationFilter.java`
- **동작**:
  1. Authorization Header에서 Bearer 토큰 추출
  2. 토큰에서 사용자명(이메일) 추출
  3. 사용자 조회
  4. 토큰 유효성 검증
  5. SecurityContext에 인증 정보 저장

### ✅ 7. OpenAPI 명세 (Spec-First)
- **스키마**:
  - `schemas/RegisterRequest.yaml`: 회원가입 요청
  - `schemas/LoginRequest.yaml`: 로그인 요청
  - `schemas/AuthResponse.yaml`: 인증 응답 (JWT 토큰)
- **경로**:
  - `paths/auth-register.yaml`: POST /api/auth/register
  - `paths/auth-login.yaml`: POST /api/auth/login
- **메인 파일**: `api-spec.yaml`에 auth 태그 추가

### ✅ 8. 인증 API Controller
- **파일**: `src/main/java/com/example/springbasic/controller/AuthController.java`
- **구현**: AuthApi 인터페이스 구현
- **변환**: API DTO ↔ Service DTO

---

## 아키텍처 개요

### 인증 흐름

```
1. 회원가입/로그인
   클라이언트
       ↓ POST /api/auth/register or /api/auth/login
   AuthController (API DTO 변환)
       ↓
   AuthService (비즈니스 로직)
       ↓
   - PasswordEncoder (BCrypt 암호화)
   - UserRepository (DB 저장/조회)
   - JwtUtil (토큰 발급)
       ↓
   JWT 토큰 반환

2. 보호된 API 접근
   클라이언트
       ↓ Authorization: Bearer {token}
   JwtAuthenticationFilter
       ↓ 토큰 추출 및 검증
   SecurityContext (인증 정보 저장)
       ↓
   Controller (인증된 사용자 정보 접근)
```

### 필터 체인

```
클라이언트 요청
    ↓
1. JwtAuthenticationFilter
    - JWT 토큰 추출
    - 토큰 검증
    - SecurityContext 설정
    ↓
2. SecurityFilterChain
    - 권한 확인 (permitAll or authenticated)
    - URL 패턴 매칭
    ↓
3. Controller
    - @AuthenticationPrincipal로 사용자 정보 접근
```

---

## 주요 컴포넌트 설명

### 1. JWT 토큰 구조

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiZXhwIjoxNjk...
│      Header       │       Payload        │      Signature      │
```

- **Header**: 알고리즘 (HMAC-SHA256)
- **Payload**: 사용자 정보 (이메일, 만료 시간)
- **Signature**: 위변조 방지 서명

### 2. BCrypt 비밀번호 암호화

```java
// 암호화
String encoded = passwordEncoder.encode("myPassword123");
// "$2a$10$abcd..."

// 검증
boolean matches = passwordEncoder.matches("myPassword123", encoded);
// true
```

- Salt 자동 생성
- 같은 비밀번호도 매번 다른 해시값
- 일방향 암호화 (복호화 불가)

### 3. Spring Security 권한 체계

- **USER**: 일반 사용자 (`ROLE_USER`)
- **ADMIN**: 관리자 (`ROLE_ADMIN`)

```java
// 메서드 레벨 권한 체크
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }
```

---

## 테스트 방법

### 사전 준비

1. **데이터베이스 시작**
   ```bash
   /usr/local/bin/docker-compose up -d
   ```

2. **마이그레이션 실행 (자동으로 실행되지만 확인용)**
   ```bash
   /usr/local/bin/docker-compose run --rm dbmate status
   ```

3. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

### 1. 회원가입 테스트

**요청:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "email": "hong@example.com",
    "age": 25,
    "password": "myPassword123"
  }'
```

**예상 응답 (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJob25nQGV4YW1wbGUuY29tIiwiZXhwIjoxNjk2NTIxNjAwfQ.abc123",
  "userId": 1,
  "email": "hong@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

**에러 케이스:**
- 409 Conflict: 이메일 중복
- 400 Bad Request: 유효성 검증 실패

### 2. 로그인 테스트

**요청:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "hong@example.com",
    "password": "myPassword123"
  }'
```

**예상 응답 (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJob25nQGV4YW1wbGUuY29tIiwiZXhwIjoxNjk2NTIxNjAwfQ.abc123",
  "userId": 1,
  "email": "hong@example.com",
  "name": "홍길동",
  "role": "USER"
}
```

**에러 케이스:**
- 401 Unauthorized: 이메일 또는 비밀번호 불일치
- 400 Bad Request: 필수 필드 누락

### 3. JWT 토큰으로 보호된 API 접근 테스트

**요청 (인증 없이):**
```bash
curl -X GET http://localhost:8080/api/users
```

**예상 응답 (403 Forbidden):**
```json
{
  "timestamp": "2025-01-20T10:30:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/users"
}
```

**요청 (JWT 토큰 포함):**
```bash
# 먼저 로그인해서 토큰 받기
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"hong@example.com","password":"myPassword123"}' \
  | jq -r '.token')

# 토큰으로 API 접근
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"
```

**예상 응답 (200 OK):**
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "email": "hong@example.com",
    "age": 25,
    "createdAt": "2025-01-20T10:30:00",
    "updatedAt": "2025-01-20T10:30:00"
  }
]
```

### 4. Swagger UI 테스트

1. **Swagger UI 접속**
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **회원가입 API 테스트**
   - `auth` 섹션의 `POST /api/auth/register` 클릭
   - "Try it out" 클릭
   - Request body 입력
   - "Execute" 클릭
   - JWT 토큰 복사

3. **JWT 토큰 설정**
   - Swagger UI 상단의 "Authorize" 버튼 클릭
   - Value에 `Bearer {복사한토큰}` 입력
   - "Authorize" 클릭

4. **보호된 API 테스트**
   - `users` 섹션의 API들 테스트
   - 인증된 상태로 요청 성공 확인

---

## 트러블슈팅

### 1. 애플리케이션 시작 실패

**증상:**
```
Error creating bean with name 'securityFilterChain'
```

**원인:** Spring Security 설정 오류

**해결:**
- SecurityConfig.java의 필터 체인 설정 확인
- JwtAuthenticationFilter Bean 등록 확인

### 2. JWT 토큰 검증 실패

**증상:**
```
JWT 인증 중 오류 발생: JWT signature does not match
```

**원인:** Secret Key 불일치

**해결:**
- application.yml의 `jwt.secret` 확인
- 최소 256비트 (32자) 이상인지 확인

### 3. 403 Forbidden (인증 토큰 있음에도)

**증상:** JWT 토큰을 보냈는데도 403 에러

**원인:**
1. Authorization Header 형식 오류
2. 토큰 만료
3. 권한 부족

**해결:**
```bash
# 1. Header 형식 확인
Authorization: Bearer eyJhbGc...  # 정확한 형식

# 2. 토큰 만료 확인 (jwt.io에서 디코딩)
# exp 필드가 현재 시간보다 미래인지 확인

# 3. 권한 확인
# ADMIN 전용 API는 ADMIN 권한 필요
```

### 4. 비밀번호 검증 실패

**증상:** 올바른 비밀번호인데도 로그인 실패

**원인:** 평문 비밀번호가 DB에 저장됨

**해결:**
- 회원가입 시 BCrypt 암호화 확인
- AuthService.register() 메서드 확인
- DB의 password 컬럼이 `$2a$10$...` 형식인지 확인

### 5. CORS 에러 (프론트엔드 연동 시)

**증상:**
```
Access to fetch at 'http://localhost:8080/api/auth/login' from origin 'http://localhost:3000' has been blocked by CORS policy
```

**해결:** SecurityConfig에 CORS 설정 추가
```java
http.cors(cors -> cors.configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("*"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    return config;
}));
```

---

## 다음 단계

### 1. 추가 기능 구현
- [ ] 토큰 갱신 (Refresh Token)
- [ ] 비밀번호 변경 API
- [ ] 이메일 인증
- [ ] OAuth2 소셜 로그인 (Google, GitHub 등)

### 2. 보안 강화
- [ ] 로그인 실패 횟수 제한
- [ ] IP 기반 접근 제한
- [ ] Rate Limiting
- [ ] 토큰 블랙리스트 (로그아웃 시)

### 3. 테스트 작성
- [ ] AuthService 단위 테스트
- [ ] AuthController 통합 테스트
- [ ] JWT 필터 테스트
- [ ] Security 설정 테스트

### 4. 모니터링
- [ ] 인증 실패 로그
- [ ] JWT 토큰 발급 통계
- [ ] 보안 이벤트 추적

---

## 참고 자료

### 프로젝트 내 문서
- `CLAUDE.md`: 개발 워크플로우 및 명령어
- `docs/SESSION_CONTEXT.md`: 세션 컨텍스트 (끊김 후 재개용)
- `docs/LEARNING_LOG.md`: 학습 기록
- `database/schema.dbml`: DB 스키마 명세

### 외부 자료
- [Spring Security 공식 문서](https://docs.spring.io/spring-security/reference/index.html)
- [JWT 공식 사이트](https://jwt.io/)
- [BCrypt 설명](https://en.wikipedia.org/wiki/Bcrypt)

---

## 요약

### 구현 완료
✅ Spring Security + JWT 인증 시스템 완성
✅ 회원가입/로그인 API (명세 우선 개발)
✅ JWT 토큰 발급 및 검증
✅ BCrypt 비밀번호 암호화
✅ UserDetails 통합

### 테스트 대기 중
⏳ 애플리케이션 실행
⏳ Postman/curl 테스트
⏳ Swagger UI 테스트

### 다음 커밋 전 체크리스트
- [ ] DB 컨테이너 실행 확인
- [ ] 애플리케이션 정상 시작 확인
- [ ] 회원가입 API 테스트 성공
- [ ] 로그인 API 테스트 성공
- [ ] JWT 토큰으로 보호된 API 접근 성공
- [ ] 예외 처리 확인 (중복 이메일, 잘못된 비밀번호 등)

**테스트가 완료되면 이 문서를 업데이트하고 커밋하세요!**
