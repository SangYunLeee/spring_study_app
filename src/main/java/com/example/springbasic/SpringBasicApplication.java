package com.example.springbasic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 애플리케이션의 시작점
 *
 * @SpringBootApplication 어노테이션은 다음 3가지를 포함합니다:
 * 1. @Configuration: 이 클래스가 설정 클래스임을 나타냄
 * 2. @EnableAutoConfiguration: Spring Boot의 자동 설정 기능을 활성화
 * 3. @ComponentScan: 현재 패키지와 하위 패키지의 컴포넌트들을 스캔
 */
@SpringBootApplication
public class SpringBasicApplication {

    public static void main(String[] args) {
        // Spring Boot 애플리케이션 시작
        SpringApplication.run(SpringBasicApplication.class, args);
    }
}
