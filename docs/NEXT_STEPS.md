# 다음 학습 단계

Spring Boot의 핵심 개념들을 체계적으로 학습해왔습니다.
명세 우선 개발부터 예외 처리, 검증, 트랜잭션, 테스트까지 실무 필수 기능들을 모두 마스터했습니다!

## 현재 학습 수준: 중급 완성 (레벨 1-8 완료) ✨

✅ Spring Boot 프로젝트 생성 및 기본 구조
✅ Controller-Service-Repository 계층 분리
✅ RESTful API 설계 (GET, POST, PUT, PATCH, DELETE)
✅ 명세 우선 개발 (OpenAPI + Liquibase)
✅ PostgreSQL + Spring Data JPA 연동
✅ 전역 예외 처리 (@RestControllerAdvice)
✅ Bean Validation (@Valid)
✅ 트랜잭션 관리 (@Transactional)
✅ 통합 테스트 작성 (@SpringBootTest)

---

## 추천 학습 경로

현재 중급 레벨을 완성했으므로, 이제 고급 주제나 실전 프로젝트로 나아갈 수 있습니다!

### 레벨 9: 단위 테스트 (Mock 사용) (중급 → 고급)

통합 테스트는 완료했으니, 이제 빠른 단위 테스트를 작성해봅니다.
Mock을 사용하여 각 계층을 독립적으로 테스트합니다.

**Claude Code에게 요청하기:**
```
Mockito를 사용한 단위 테스트 작성법을 배우고 싶어
```

**배울 내용:**
- Mockito 기본 사용법
- @Mock, @InjectMocks 어노테이션
- when().thenReturn() 패턴
- Controller, Service 단위 테스트
- MockMvc를 사용한 Controller 테스트
- 테스트 커버리지 향상

**실습 예제:**
- UserService 단위 테스트 (Repository Mock)
- UsersApiController 단위 테스트 (Service Mock)
- 엣지 케이스 테스트

**예상 소요 시간:** 1-1.5시간

**왜 필요한가?**
- 통합 테스트는 느림 (DB 연결, Spring 컨텍스트 로딩)
- 단위 테스트는 빠름 (Mock 객체 사용)
- TDD (Test-Driven Development) 가능

---

### 레벨 10: API 문서 자동화 (Swagger UI) (중급)

OpenAPI 명세를 시각화하여 API 문서를 자동 생성합니다.
프론트엔드 개발자나 다른 팀원이 쉽게 API를 이해할 수 있습니다.

**Claude Code에게 요청하기:**
```
Swagger UI로 API 문서를 자동 생성하고 싶어
```

**배울 내용:**
- Springdoc OpenAPI 라이브러리
- Swagger UI 설정
- API 명세 기반 문서 자동 생성
- 브라우저에서 API 테스트

**실습 예제:**
- Swagger UI 설정
- /swagger-ui.html 접속
- 브라우저에서 직접 API 호출

**예상 소요 시간:** 30-45분

**장점:**
- 명세(api-spec.yaml)만 작성하면 문서 자동 생성
- 팀원들과 API 공유 용이
- Postman 대신 브라우저에서 테스트 가능

---

### 레벨 11: Spring Security 기본 (인증) (고급)

실제 서비스는 로그인/회원가입이 필요합니다.
Spring Security로 기본적인 인증을 구현합니다.

**Claude Code에게 요청하기:**
```
Spring Security로 로그인 기능을 구현하고 싶어
```

**배울 내용:**
- Spring Security 기본 개념
- 비밀번호 암호화 (BCrypt)
- 로그인/회원가입 API
- JWT 토큰 기반 인증 (또는 세션 기반)
- SecurityFilterChain 설정

**실습 예제:**
- 회원가입 API (비밀번호 암호화)
- 로그인 API (JWT 발급)
- 인증이 필요한 API 보호

**예상 소요 시간:** 2-3시간

**주의:**
- Spring Security는 복잡함
- 차근차근 단계별로 진행 필요

---

### 레벨 12: 관계형 데이터 모델링 (중급 → 고급)

현재는 User 엔티티 하나만 있습니다.
실제 서비스는 여러 엔티티 간의 관계가 필요합니다.

**Claude Code에게 요청하기:**
```
JPA 연관관계 매핑을 배우고 싶어. 게시글과 댓글 예제로 시작하자
```

**배울 내용:**
- @OneToMany, @ManyToOne
- @ManyToMany, @OneToOne
- 양방향 vs 단방향 관계
- FetchType (LAZY, EAGER)
- orphanRemoval, cascade 옵션
- N+1 문제와 해결 방법

**실습 예제:**
1. **게시글 + 댓글**
   - Post (1) : Comment (N)
   - 게시글 조회 시 댓글 목록 함께 조회

2. **사용자 + 게시글**
   - User (1) : Post (N)
   - 작성자 정보 포함

3. **게시글 + 태그**
   - Post (N) : Tag (N)
   - 다대다 관계

**예상 소요 시간:** 2-3시간

---

### 레벨 13: 성능 최적화 (고급)

실전에서는 성능이 중요합니다.
쿼리 최적화, 캐싱, 인덱싱 등을 배웁니다.

**Claude Code에게 요청하기:**
```
N+1 문제를 해결하고 쿼리 성능을 최적화하고 싶어
```

**배울 내용:**
- N+1 문제 이해 및 해결 (fetch join, @EntityGraph)
- 쿼리 로그 분석
- DB 인덱스 설계 (Liquibase)
- Spring Cache (@Cacheable)
- 페이징 최적화

**실습 예제:**
- N+1 발생 코드 → fetch join으로 해결
- 자주 조회되는 데이터 캐싱
- 검색 성능을 위한 인덱스 추가

**예상 소요 시간:** 2-3시간

---

## 실전 프로젝트 (종합)

배운 내용을 모두 활용하여 완전한 애플리케이션을 만들어봅니다.

### 프로젝트 아이디어 1: 블로그 플랫폼

**Claude Code에게 요청하기:**
```
배운 내용을 종합해서 블로그 플랫폼을 만들고 싶어
```

**기능:**
1. 회원가입/로그인 (Spring Security + JWT)
2. 게시글 CRUD (User ↔ Post 관계)
3. 댓글 기능 (Post ↔ Comment 관계)
4. 태그/카테고리 (ManyToMany 관계)
5. 페이징 및 검색
6. 이미지 업로드
7. 좋아요 기능

**활용 기술:**
- 명세 우선 개발 (OpenAPI + Liquibase)
- 전역 예외 처리
- Bean Validation
- 트랜잭션 관리
- 통합 테스트 + 단위 테스트
- Swagger UI 문서화

---

### 프로젝트 아이디어 2: TODO 관리 + 팀 협업

**Claude Code에게 요청하기:**
```
팀 협업 기능이 있는 TODO 관리 앱을 만들고 싶어
```

**기능:**
1. 회원가입/로그인
2. 개인 TODO 관리
3. 팀 생성 및 공유 TODO
4. 담당자 할당
5. 마감일 알림
6. 완료율 통계

**활용 기술:**
- User ↔ Team (ManyToMany)
- Team ↔ Todo (OneToMany)
- User ↔ Todo (ManyToOne, 담당자)
- 복잡한 쿼리 최적화

---

### 프로젝트 아이디어 3: 간단한 쇼핑몰 API

**Claude Code에게 요청하기:**
```
간단한 쇼핑몰 백엔드를 만들고 싶어
```

**기능:**
1. 상품 관리 (CRUD)
2. 장바구니 (User ↔ Cart ↔ Product)
3. 주문 처리 (Order ↔ OrderItem)
4. 결제 연동 (외부 API)
5. 재고 관리 (트랜잭션)
6. 주문 통계

---

## 추가 고급 주제 (선택 사항)

더 깊이 배우고 싶다면:

### 배포 및 DevOps
```
Docker로 애플리케이션을 배포하고 싶어
```
- Dockerfile 작성
- Docker Compose (App + PostgreSQL)
- AWS/GCP 배포
- CI/CD (GitHub Actions)

### 외부 API 연동
```
외부 API를 호출하는 방법을 배우고 싶어
```
- RestTemplate vs WebClient
- 날씨 API 연동 예제
- OAuth 2.0 소셜 로그인 (Google, Kakao)
- 결제 API (Stripe, Toss Payments)

### 파일 업로드/다운로드
```
파일 업로드 기능을 구현하고 싶어
```
- MultipartFile
- 이미지 업로드/다운로드
- AWS S3 연동
- 파일 저장 전략 (로컬 vs 클라우드)

### 비동기 처리
```
비동기 작업 처리를 배우고 싶어
```
- @Async 어노테이션
- CompletableFuture
- 이메일 발송 (비동기)
- 대용량 데이터 처리

### 메시징 시스템
```
메시징 큐를 사용하고 싶어
```
- RabbitMQ 또는 Kafka
- 이벤트 기반 아키텍처
- 주문 처리 비동기화

---

## 학습 팁

### 1. 현재 수준에 맞는 주제 선택
- ✅ **완료**: 레벨 1-8 (기본 → 중급)
- 🎯 **추천**: 레벨 9 (단위 테스트) 또는 레벨 10 (Swagger UI)
- ⚠️ **나중에**: 레벨 11+ (고급 주제)

### 2. 실습 중심으로 배우세요
- 개념만 읽지 말고 직접 코드를 작성해보세요
- 에러가 나면 해결 과정에서 더 많이 배웁니다
- 테스트 코드를 작성하면 이해도가 높아집니다

### 3. Claude Code 활용하기
- 막히는 부분이 있으면 언제든지 질문하세요:
  ```
  @Transactional의 readOnly 옵션이 정확히 어떤 최적화를 하는지 설명해줘
  ```

- 코드 설명 요청:
  ```
  GlobalExceptionHandler가 어떻게 동작하는지 한 줄씩 설명해줘
  ```

- 에러 해결:
  ```
  MethodArgumentNotValidException이 왜 발생했는지 설명하고 해결 방법을 알려줘
  ```

- 리팩토링:
  ```
  이 코드를 더 깔끔하게 리팩토링해줘
  ```

### 4. 학습 기록 남기기
- 새로운 내용을 배울 때마다:
  ```
  LEARNING_LOG.md에 오늘 배운 내용을 추가해줘
  ```

### 5. 명세 우선 개발 유지
- API 변경 시: `api-spec.yaml` 먼저 수정 → `./gradlew generateApi`
- DB 변경 시: Liquibase changeset 작성 → `./gradlew bootRun`
- 코드를 먼저 수정하지 마세요!

---

## 다음 세션 시작하기

새로운 Claude Code 세션을 시작할 때:

```
LEARNING_LOG.md를 읽어주고, 내가 어디까지 배웠는지 요약해줘
```

그 다음:

```
NEXT_STEPS.md를 보고 다음 학습할 내용을 추천해줘
```

또는 직접 원하는 주제 선택:

```
레벨 9의 단위 테스트를 배우고 싶어. Mockito 사용법을 알려줘
```

---

## 현재 추천 학습 주제

### 🎯 가장 추천: 레벨 9 (단위 테스트)

통합 테스트는 작성했지만, 단위 테스트는 아직 없습니다.
Mock을 사용한 빠른 테스트 작성법을 배우는 것을 추천합니다.

**시작하려면:**
```
Mockito를 사용한 단위 테스트 작성법을 배우고 싶어
```

**이유:**
1. 실무에서 단위 테스트는 필수
2. 통합 테스트는 느림 → 단위 테스트로 보완
3. TDD (테스트 주도 개발) 가능
4. 리팩토링 시 안전망 역할

---

### 🎨 또는: 레벨 10 (Swagger UI)

API 문서를 자동 생성하여 팀원들과 공유하고 싶다면:

**시작하려면:**
```
Swagger UI로 API 문서를 자동 생성하고 싶어
```

**이유:**
1. OpenAPI 명세를 시각화
2. 브라우저에서 API 테스트 가능
3. 프론트엔드 개발자와 협업 용이
4. 빠르게 완성 가능 (30-45분)

---

## 이미 구현된 기능 (복습용)

현재 프로젝트에는 이미 많은 기능이 구현되어 있습니다:

- ✅ **명세 우선 개발**: OpenAPI 명세 → 코드 자동 생성
- ✅ **DB 스키마 관리**: Liquibase changeset
- ✅ **전역 예외 처리**: @RestControllerAdvice
- ✅ **자동 검증**: @Valid + Bean Validation
- ✅ **트랜잭션 관리**: @Transactional (readOnly 최적화)
- ✅ **페이징**: Spring Data JPA Pageable
- ✅ **통합 테스트**: @SpringBootTest + 실제 DB

**실행 방법:**
```bash
# 1. PostgreSQL 시작
docker compose up -d

# 2. 애플리케이션 실행
./gradlew bootRun

# 3. API 테스트
curl http://localhost:8080/api/users
```

행복한 Spring 학습 되세요! 🚀