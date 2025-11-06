package com.example.springbasic.controller;

import com.example.springbasic.service.CalculatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 계산기 API 컨트롤러
 *
 * Service를 주입받아 사용하는 방법을 보여줍니다
 */
@RestController
@RequestMapping("/calculator")  // 이 컨트롤러의 모든 엔드포인트는 /calculator로 시작
public class CalculatorController {

    /**
     * 의존성 주입 방법 1: 생성자 주입 (권장 방법!)
     *
     * 장점:
     * 1. 불변성(immutable) - final로 선언 가능
     * 2. 필수 의존성 보장 - 생성 시점에 반드시 주입됨
     * 3. 테스트 용이 - Mock 객체 주입이 쉬움
     * 4. 순환 참조 방지 - 컴파일 시점에 감지 가능
     *
     * Spring 4.3 이후부터는 생성자가 하나만 있으면
     * @Autowired를 생략해도 자동으로 주입됩니다
     */
    private final CalculatorService calculatorService;

    // 생성자 주입 - @Autowired 생략 가능
    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    /**
     * 덧셈 API
     * 예: GET /calculator/add?a=10&b=5
     * 응답: {"result": 15}
     */
    @GetMapping("/add")
    public CalculationResponse add(
            @RequestParam int a,
            @RequestParam int b
    ) {
        int result = calculatorService.add(a, b);
        return new CalculationResponse("addition", a, b, result);
    }

    /**
     * 뺄셈 API
     * 예: GET /calculator/subtract?a=10&b=5
     * 응답: {"result": 5}
     */
    @GetMapping("/subtract")
    public CalculationResponse subtract(
            @RequestParam int a,
            @RequestParam int b
    ) {
        int result = calculatorService.subtract(a, b);
        return new CalculationResponse("subtraction", a, b, result);
    }

    /**
     * 곱셈 API
     * 예: GET /calculator/multiply?a=10&b=5
     * 응답: {"result": 50}
     */
    @GetMapping("/multiply")
    public CalculationResponse multiply(
            @RequestParam int a,
            @RequestParam int b
    ) {
        int result = calculatorService.multiply(a, b);
        return new CalculationResponse("multiplication", a, b, result);
    }

    /**
     * 나눗셈 API
     * 예: GET /calculator/divide?a=10&b=5
     * 응답: {"result": 2.0}
     */
    @GetMapping("/divide")
    public DivisionResponse divide(
            @RequestParam int a,
            @RequestParam int b
    ) {
        double result = calculatorService.divide(a, b);
        return new DivisionResponse("division", a, b, result);
    }

    /**
     * 평균 계산 API
     * 예: GET /calculator/average?numbers=10,20,30,40,50
     * 응답: {"average": 30.0, "count": 5}
     */
    @GetMapping("/average")
    public AverageResponse average(@RequestParam int[] numbers) {
        double avg = calculatorService.calculateAverage(numbers);
        return new AverageResponse(avg, numbers.length);
    }

    /**
     * 계산 결과를 담는 응답 객체 (정수 결과용)
     */
    record CalculationResponse(String operation, int a, int b, double result) {
    }

    /**
     * 나눗셈 결과를 담는 응답 객체 (실수 결과용)
     */
    record DivisionResponse(String operation, int a, int b, double result) {
    }

    /**
     * 평균 계산 결과를 담는 응답 객체
     */
    record AverageResponse(double average, int count) {
    }
}