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
                        .title("Spring Boot 학습 프로젝트 API")
                        .version("1.0.0")
                        .description("""
                                # Spring Boot 기초 학습용 RESTful API

                                이 API는 Spring Boot를 학습하기 위한 프로젝트입니다.

                                ## 주요 기능
                                - 사용자 관리 (CRUD)
                                - 계산기 서비스

                                ## 학습 내용
                                - RESTful API 설계
                                - Controller-Service-Repository 패턴
                                - DTO 패턴
                                - HTTP 메서드 (GET, POST, PUT, PATCH, DELETE)

                                ## 기술 스택
                                - Spring Boot 3.2.0
                                - Java 21
                                - Gradle
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