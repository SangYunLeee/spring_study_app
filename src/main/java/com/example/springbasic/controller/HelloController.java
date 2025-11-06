package com.example.springbasic.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 첫 번째 컨트롤러 - HTTP 요청을 처리합니다
 *
 * @RestController: 이 클래스가 REST API 컨트롤러임을 나타냅니다
 * - @Controller + @ResponseBody 를 합친 것
 * - 반환값이 자동으로 JSON으로 변환되어 HTTP 응답 본문에 작성됩니다
 */
@RestController
public class HelloController {

    /**
     * 가장 기본적인 엔드포인트
     * http://localhost:8080/hello 로 접속하면 "Hello, Spring!" 메시지가 반환됩니다
     *
     * @GetMapping: HTTP GET 요청을 처리하는 메서드임을 나타냅니다
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Spring!";
    }

    /**
     * 쿼리 파라미터를 받는 엔드포인트
     * 예: http://localhost:8080/greet?name=철수
     * 결과: "안녕하세요, 철수님!"
     *
     * @RequestParam: URL의 쿼리 파라미터 값을 받습니다
     * defaultValue: 파라미터가 없을 때 사용할 기본값
     */
    @GetMapping("/greet")
    public String greet(@RequestParam(defaultValue = "Guest") String name) {
        return "안녕하세요, " + name + "님!";
    }

    /**
     * 경로 변수를 받는 엔드포인트
     * 예: http://localhost:8080/welcome/영희
     * 결과: "환영합니다, 영희님!"
     *
     * @PathVariable: URL 경로의 일부를 변수로 받습니다
     */
    @GetMapping("/welcome/{name}")
    public String welcome(@PathVariable String name) {
        return "환영합니다, " + name + "님!";
    }

    /**
     * JSON 객체를 반환하는 엔드포인트
     * 결과: {"message": "Hello", "timestamp": 1234567890}
     */
    @GetMapping("/info")
    public MessageResponse getInfo() {
        return new MessageResponse("Hello", System.currentTimeMillis());
    }

    /**
     * 간단한 응답 객체
     * record는 Java 16부터 지원되는 불변 데이터 클래스
     */
    record MessageResponse(String message, long timestamp) {
    }
}
