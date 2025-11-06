# Spring Boot 기초 학습 프로젝트

Spring Framework를 처음부터 배우는 학습용 프로젝트입니다.

## 프로젝트 정보

- **Java 버전**: 21
- **Spring Boot 버전**: 3.2.0
- **빌드 도구**: Gradle 8.14.2
- **포트**: 8080

## 프로젝트 구조

```
spring_app/
├── README.md                           # 프로젝트 설명서 (이 파일)
├── LEARNING_LOG.md                     # 학습 진행 상황 기록
├── NEXT_STEPS.md                       # 다음 학습 단계 안내
├── build.gradle                        # Gradle 빌드 설정
├── settings.gradle                     # Gradle 프로젝트 설정
└── src/
    └── main/
        ├── java/com/example/springbasic/
        │   ├── SpringBasicApplication.java      # 메인 애플리케이션
        │   └── controller/
        │       └── HelloController.java         # REST API 컨트롤러
        └── resources/
            └── application.properties           # 애플리케이션 설정
```

## 실행 방법

### 1. 애플리케이션 시작

```bash
./gradlew bootRun
```

### 2. 애플리케이션 중지

터미널에서 `Ctrl + C`를 누르거나, 백그라운드로 실행 중이라면:

```bash
# 실행 중인 프로세스 찾기
lsof -i :8080

# 프로세스 종료 (PID는 위 명령어로 확인)
kill -9 [PID]
```

### 3. 빌드만 하기

```bash
./gradlew build
```

### 4. 테스트 실행

```bash
./gradlew test
```

## API 엔드포인트 테스트

애플리케이션이 실행 중일 때 다음 엔드포인트를 테스트할 수 있습니다:

### 1. 기본 Hello World

```bash
curl http://localhost:8080/hello
# 응답: Hello, Spring!
```

브라우저: http://localhost:8080/hello

### 2. 쿼리 파라미터 사용

```bash
curl "http://localhost:8080/greet?name=철수"
# 응답: 안녕하세요, 철수님!

curl http://localhost:8080/greet
# 응답: 안녕하세요, Guest님!
```

브라우저: http://localhost:8080/greet?name=철수

### 3. 경로 변수 사용

```bash
curl http://localhost:8080/welcome/영희
# 응답: 환영합니다, 영희님!
```

브라우저: http://localhost:8080/welcome/영희

### 4. JSON 응답

```bash
curl http://localhost:8080/info
# 응답: {"message":"Hello","timestamp":1234567890}
```

브라우저: http://localhost:8080/info

## 핵심 개념 정리

### Spring Boot의 주요 어노테이션

| 어노테이션 | 위치 | 설명 |
|----------|------|------|
| `@SpringBootApplication` | 메인 클래스 | Spring Boot 애플리케이션의 시작점 표시 |
| `@RestController` | 클래스 | REST API 컨트롤러 표시, JSON 응답 자동 변환 |
| `@GetMapping` | 메서드 | HTTP GET 요청 처리 |
| `@RequestParam` | 파라미터 | URL 쿼리 파라미터 받기 (?name=value) |
| `@PathVariable` | 파라미터 | URL 경로의 변수 받기 (/user/{id}) |

### 파일별 역할

#### [build.gradle](build.gradle)
- 프로젝트 의존성(dependencies) 관리
- Spring Boot 버전 설정
- 플러그인 설정

#### [SpringBasicApplication.java](src/main/java/com/example/springbasic/SpringBasicApplication.java)
- 애플리케이션의 시작점 (main 메서드)
- `@SpringBootApplication` 어노테이션으로 Spring Boot 설정

#### [HelloController.java](src/main/java/com/example/springbasic/controller/HelloController.java)
- HTTP 요청을 받아서 처리하는 컨트롤러
- 4가지 엔드포인트 예제 포함

#### [application.properties](src/main/resources/application.properties)
- 서버 포트, 로그 레벨 등 애플리케이션 설정

## 학습 진행 상황

현재까지 학습한 내용은 [LEARNING_LOG.md](LEARNING_LOG.md)에서 확인하세요.

다음 학습할 주제는 [NEXT_STEPS.md](NEXT_STEPS.md)에서 확인하세요.

## Claude Code로 계속 학습하기

이 프로젝트는 Claude Code와 함께 학습하고 있습니다. 세션이 끊긴 후에도 계속 학습하려면:

### 1. 이전 학습 내용 확인

```
LEARNING_LOG.md 파일을 읽어줘
```

### 2. 다음 단계 확인

```
NEXT_STEPS.md 파일을 읽어주고, 다음 단계를 진행해줘
```

### 3. 새로운 기능 추가 요청 예시

```
Service 레이어를 추가해서 비즈니스 로직을 분리하고 싶어
```

```
데이터베이스(H2)를 연동하고 JPA를 사용해서 데이터를 저장하고 싶어
```

```
POST 요청으로 데이터를 받아서 처리하는 방법을 배우고 싶어
```

### 4. 코드 설명 요청

```
HelloController.java 파일을 열고 각 메서드가 어떻게 동작하는지 설명해줘
```

## 문제 해결

### 포트가 이미 사용 중일 때

```bash
# 포트 8080을 사용하는 프로세스 찾기
lsof -i :8080

# 해당 프로세스 종료
kill -9 [PID]
```

또는 [application.properties](src/main/resources/application.properties)에서 포트 변경:

```properties
server.port=8081
```

### Gradle 빌드 오류

```bash
# Gradle 캐시 정리
./gradlew clean

# 다시 빌드
./gradlew build
```

## 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Boot 가이드](https://spring.io/guides)
- [Baeldung Spring 튜토리얼](https://www.baeldung.com/spring-boot)

## 프로젝트 정보

- **생성일**: 2025-11-06
- **학습 도구**: Claude Code
- **목적**: Spring Framework 기초 학습