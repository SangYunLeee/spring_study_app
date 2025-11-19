package com.example.springbasic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 설정
 *
 * 명세 기반 개발(Specification-Driven Development)을 위한 설정
 * - API 명세를 자동으로 생성
 * - Swagger UI를 통해 브라우저에서 API 문서 확인 및 테스트 가능
 *
 * 접속 URL:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - API Docs (JSON): http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot User API")
                        .version("1.0.0")
                        .description("""
                                # 명세 우선 개발 (Spec-First Development) API

                                이 API는 OpenAPI 명세로부터 자동 생성되었습니다.

                                ## 개발 워크플로우
                                1. `src/main/resources/openapi/api-spec.yaml` 수정
                                2. `./gradlew generateApi` 실행
                                3. 자동 생성된 인터페이스 구현 (Controller)
                                4. Swagger UI에서 확인 및 테스트

                                ## 주요 기능
                                - ✅ RESTful API (CRUD)
                                - ✅ 페이징 및 정렬
                                - ✅ Bean Validation (@Valid)
                                - ✅ 전역 예외 처리 (@RestControllerAdvice)
                                - ✅ 트랜잭션 관리 (@Transactional)
                                - ✅ 단위 테스트 (Mockito)
                                - ✅ 통합 테스트 (Testcontainers)

                                ## 기술 스택
                                - Spring Boot 3.2.0
                                - Java 21
                                - PostgreSQL 16 + dbmate
                                - Spring Data JPA
                                - OpenAPI 3.0.3
                                """)
                        .contact(new Contact()
                                .name("학습자")
                                .email("learner@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("로컬 개발 서버")
                ));
    }
}