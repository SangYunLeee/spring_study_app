package com.example.springbasic.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 의존성 주입(Dependency Injection)의 3가지 방법 예제
 *
 * 이 파일은 학습용 예제입니다. 실제 프로젝트에서는 사용하지 않습니다.
 */
public class DependencyInjectionExamples {

    /**
     * 예제 서비스 클래스
     */
    @Component
    static class ExampleService {
        public String getMessage() {
            return "Hello from Service!";
        }
    }

    // ========================================
    // 방법 1: 생성자 주입 (Constructor Injection) - 권장!
    // ========================================

    /**
     * 생성자 주입 예제
     *
     * 장점:
     * 1. final 사용 가능 → 불변성 보장
     * 2. 필수 의존성 명확히 표현
     * 3. 테스트하기 쉬움 (Mock 객체 주입)
     * 4. 순환 참조 방지 (컴파일 시점 감지)
     * 5. Spring 4.3+ 에서는 @Autowired 생략 가능
     *
     * 단점:
     * - 없음 (이 방법을 사용하세요!)
     */
    @Component
    static class ConstructorInjectionExample {
        private final ExampleService service;  // final 사용 가능!

        // @Autowired 생략 가능 (생성자가 하나인 경우)
        public ConstructorInjectionExample(ExampleService service) {
            this.service = service;
        }

        public void doSomething() {
            System.out.println(service.getMessage());
        }
    }

    // ========================================
    // 방법 2: 필드 주입 (Field Injection) - 비권장
    // ========================================

    /**
     * 필드 주입 예제
     *
     * 장점:
     * - 코드가 간결함
     *
     * 단점:
     * 1. final 사용 불가 → 가변 객체
     * 2. 테스트하기 어려움 (리플렉션 필요)
     * 3. 순환 참조 발견이 런타임에만 가능
     * 4. 의존성이 숨겨짐 (생성자에 드러나지 않음)
     * 5. IntelliJ에서 경고 표시
     *
     * 사용 예외:
     * - 테스트 코드에서만 사용 (@SpringBootTest)
     * - 레거시 코드 유지보수
     */
    @Component
    static class FieldInjectionExample {
        @Autowired
        private ExampleService service;  // final 사용 불가!

        public void doSomething() {
            System.out.println(service.getMessage());
        }
    }

    // ========================================
    // 방법 3: Setter 주입 (Setter Injection) - 선택적 의존성에만 사용
    // ========================================

    /**
     * Setter 주입 예제
     *
     * 장점:
     * - 선택적 의존성 표현 가능
     * - 객체 생성 후 의존성 변경 가능
     *
     * 단점:
     * 1. final 사용 불가
     * 2. 필수 의존성 표현 불가능
     * 3. 객체가 불완전한 상태로 생성될 수 있음
     *
     * 사용 시기:
     * - 선택적(optional) 의존성만 사용
     * - 예: 로깅, 모니터링 등
     */
    @Component
    static class SetterInjectionExample {
        private ExampleService service;  // final 사용 불가!

        @Autowired  // setter에는 @Autowired 필수
        public void setService(ExampleService service) {
            this.service = service;
        }

        public void doSomething() {
            if (service != null) {  // null 체크 필요!
                System.out.println(service.getMessage());
            }
        }
    }

    // ========================================
    // 실전 예제: 여러 의존성을 주입받는 경우
    // ========================================

    @Component
    static class ServiceA {
        public String getName() {
            return "Service A";
        }
    }

    @Component
    static class ServiceB {
        public String getName() {
            return "Service B";
        }
    }

    @Component
    static class ServiceC {
        public String getName() {
            return "Service C";
        }
    }

    /**
     * 여러 의존성을 주입받는 좋은 예
     */
    @Component
    static class MultiDependencyGoodExample {
        private final ServiceA serviceA;
        private final ServiceB serviceB;
        private final ServiceC serviceC;

        // 모든 의존성을 생성자로 주입
        public MultiDependencyGoodExample(
                ServiceA serviceA,
                ServiceB serviceB,
                ServiceC serviceC
        ) {
            this.serviceA = serviceA;
            this.serviceB = serviceB;
            this.serviceC = serviceC;
        }

        public void execute() {
            System.out.println(serviceA.getName());
            System.out.println(serviceB.getName());
            System.out.println(serviceC.getName());
        }
    }

    /**
     * 여러 의존성을 주입받는 나쁜 예 (필드 주입)
     */
    @Component
    static class MultiDependencyBadExample {
        @Autowired
        private ServiceA serviceA;

        @Autowired
        private ServiceB serviceB;

        @Autowired
        private ServiceC serviceC;

        public void execute() {
            System.out.println(serviceA.getName());
            System.out.println(serviceB.getName());
            System.out.println(serviceC.getName());
        }
    }

    // ========================================
    // 정리
    // ========================================

    /**
     * 의존성 주입 방법 선택 가이드:
     *
     * 1. 필수 의존성 → 생성자 주입 (99%의 경우)
     * 2. 선택적 의존성 → Setter 주입 (1%의 경우)
     * 3. 필드 주입 → 사용하지 말 것 (테스트 코드 제외)
     *
     * 결론: 거의 모든 경우에 생성자 주입을 사용하세요!
     */
}