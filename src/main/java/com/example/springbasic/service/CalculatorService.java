package com.example.springbasic.service;

import org.springframework.stereotype.Service;

/**
 * 계산 로직을 담당하는 서비스 클래스
 *
 * @Service: 이 클래스가 비즈니스 로직을 담당하는 서비스임을 Spring에게 알림
 * - Spring이 자동으로 이 클래스의 인스턴스를 생성하고 관리함 (Bean 등록)
 * - 다른 클래스에서 이 서비스를 주입받아 사용할 수 있음
 */
@Service
public class CalculatorService {

    /**
     * 두 수를 더하는 비즈니스 로직
     */
    public int add(int a, int b) {
        return a + b;
    }

    /**
     * 두 수를 빼는 비즈니스 로직
     */
    public int subtract(int a, int b) {
        return a - b;
    }

    /**
     * 두 수를 곱하는 비즈니스 로직
     */
    public int multiply(int a, int b) {
        return a * b;
    }

    /**
     * 두 수를 나누는 비즈니스 로직
     * 0으로 나누는 경우 예외 발생
     */
    public double divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("0으로 나눌 수 없습니다");
        }
        return (double) a / b;
    }

    /**
     * 복잡한 계산 예제: 여러 수의 평균 계산
     */
    public double calculateAverage(int... numbers) {
        if (numbers.length == 0) {
            throw new IllegalArgumentException("최소 하나의 숫자가 필요합니다");
        }

        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }

        return (double) sum / numbers.length;
    }
}