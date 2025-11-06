# 명세 기반 개발 (Specification-Driven Development)

코드가 아닌 **명세(Specification)**가 개발의 중심이 되는 접근 방식입니다.

## 🎯 왜 명세 기반 개발인가?

### 기존 방식의 문제점
```java
// 코드를 보고 API를 이해해야 함
@PostMapping("/api/users")
public ResponseEntity<User> create(@RequestBody CreateUserRequest request) {
    // 어떤 필드가 필수인지?
    // 어떤 검증이 있는지?
    // 어떤 상태 코드를 반환하는지?
    // 코드를 읽어봐야 알 수 있음...
}
```

### 명세 기반 개발
```
먼저 명세를 작성 → 명세를 보고 API 이해 → 명세대로 코드 구현
```

## 📋 OpenAPI (Swagger) 소개

**OpenAPI Specification (OAS)**는 RESTful API를 설명하는 표준 형식입니다.

### 장점
1. **가독성**: 다이어그램과 문서로 API 이해
2. **테스트**: 브라우저에서 바로 API 테스트 가능
3. **자동 생성**: 코드에서 자동으로 명세 생성
4. **표준**: 업계 표준으로 다른 도구와 호환
5. **클라이언트 생성**: 명세에서 자동으로 클라이언트 코드 생성 가능

### 제공되는 UI
- **Swagger UI**: 대화형 API 문서
- **ReDoc**: 깔끔한 읽기 전용 문서

## 🚀 프로젝트에서 사용하기

### 1. 의존성 추가 (완료 ✅)

[build.gradle](build.gradle):
```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
```

### 2. 설정 추가 (완료 ✅)

[OpenApiConfig.java](src/main/java/com/example/springbasic/config/OpenApiConfig.java):
- API 제목, 설명, 버전 설정
- 연락처 정보
- 라이센스 정보
- 서버 URL

### 3. 접속 URL

| 항목 | URL | 설명 |
|------|-----|------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | 대화형 API 문서 (추천!) |
| **API Docs (JSON)** | http://localhost:8080/v3/api-docs | OpenAPI 스펙 JSON |
| **API Docs (YAML)** | http://localhost:8080/v3/api-docs.yaml | OpenAPI 스펙 YAML |

## 📱 Swagger UI 사용법

### 브라우저에서 접속
```
http://localhost:8080/swagger-ui.html
```

### 주요 기능

1. **API 목록 확인**
   - 모든 엔드포인트가 그룹별로 정리됨
   - HTTP 메서드별 색상 구분 (GET: 파란색, POST: 초록색, etc.)

2. **API 상세 정보**
   - 파라미터 설명
   - 요청 본문 스키마
   - 응답 스키마
   - HTTP 상태 코드

3. **직접 테스트**
   - "Try it out" 버튼 클릭
   - 파라미터 입력
   - "Execute" 실행
   - 응답 확인

4. **스키마 확인**
   - Schemas 섹션에서 모든 DTO 구조 확인

## 🎨 명세 강화하기

### Controller에 어노테이션 추가

```java
@Tag(name = "사용자 관리", description = "사용자 CRUD API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Operation(
        summary = "사용자 생성",
        description = "새로운 사용자를 생성합니다. 이메일은 중복될 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "사용자 생성 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 요청 (중복 이메일, 잘못된 형식 등)"
        )
    })
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @Parameter(description = "생성할 사용자 정보", required = true)
        @RequestBody CreateUserRequest request
    ) {
        // ...
    }
}
```

### DTO에 스키마 설명 추가

```java
@Schema(description = "사용자 생성 요청")
public record CreateUserRequest(
    @Schema(description = "사용자 이름", example = "홍길동", required = true)
    String name,

    @Schema(description = "이메일 주소", example = "hong@example.com", required = true)
    String email,

    @Schema(description = "나이", example = "25", minimum = "0", maximum = "150", required = true)
    int age
) {}
```

## 📊 명세 기반 개발 워크플로우

```
1. 요구사항 분석
   ↓
2. OpenAPI 명세 작성/검토
   ↓
3. Swagger UI에서 명세 확인
   ↓
4. 프론트엔드/백엔드 합의
   ↓
5. 명세대로 구현
   ↓
6. Swagger UI에서 테스트
   ↓
7. 명세 자동 업데이트
```

## 🔄 명세 우선 vs 코드 우선

### 코드 우선 (Code-First) - 현재 방식
```
코드 작성 → 어노테이션 추가 → 명세 자동 생성
```

**장점:**
- 빠른 개발
- 코드와 명세 자동 동기화

**단점:**
- 구현 먼저, 설계 나중

### 명세 우선 (Spec-First)
```
명세 작성 → 명세 검토 → 명세대로 구현
```

**장점:**
- 설계 먼저
- API 계약 명확
- 프론트엔드와 병렬 개발 가능

**단점:**
- 초기 설정 복잡

## 💡 실전 활용 예시

### 시나리오: 새 API 개발

1. **팀 미팅**: "사용자 프로필 이미지 업로드 API 필요"

2. **Swagger UI 확인**: 기존 API 구조 참고

3. **명세 작성** (어노테이션으로):
```java
@Operation(summary = "프로필 이미지 업로드")
@ApiResponse(responseCode = "200", description = "업로드 성공")
@PostMapping("/api/users/{id}/profile-image")
public ResponseEntity<ImageUploadResponse> uploadProfileImage(
    @PathVariable Long id,
    @RequestParam("file") MultipartFile file
)
```

4. **Swagger UI에서 확인**: 프론트엔드 개발자와 함께 검토

5. **구현**: 명세대로 코드 작성

6. **테스트**: Swagger UI에서 바로 테스트

## 🎯 현재 프로젝트의 명세

### 자동 생성된 내용
- ✅ 모든 API 엔드포인트
- ✅ HTTP 메서드
- ✅ 파라미터 (Path, Query, Body)
- ✅ 요청/응답 스키마
- ✅ DTO 구조

### 추가하면 좋은 내용
- 📝 각 API의 상세 설명
- 📝 예제 값
- 📝 에러 응답 상세
- 📝 인증 정보 (나중에)

## 📚 다음 단계

1. **Swagger UI 확인**
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **API 테스트**
   - Try it out 기능 사용
   - 각 엔드포인트 직접 실행

3. **명세 개선** (선택)
   - Controller에 @Operation, @ApiResponse 추가
   - DTO에 @Schema 추가

4. **팀과 공유**
   - Swagger UI URL 공유
   - 명세 기반으로 협업

## 🔗 관련 링크

- [OpenAPI Specification](https://swagger.io/specification/)
- [Springdoc OpenAPI](https://springdoc.org/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

---

**다음 학습:**
- [API_TEST_GUIDE.md](API_TEST_GUIDE.md) - curl 기반 테스트
- [RESTFUL_API.md](RESTFUL_API.md) - RESTful 설계 원칙

이제 **명세를 보면 모든 API를 이해할 수 있습니다!** 🎉