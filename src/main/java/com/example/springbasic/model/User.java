package com.example.springbasic.model;

/**
 * 사용자 정보를 담는 모델 클래스
 *
 * record를 사용하여 불변 데이터 클래스를 간단하게 정의
 * - 자동으로 생성자, getter, equals, hashCode, toString 생성
 * - Java 16 이상에서 사용 가능
 */
public record User(
        Long id,
        String name,
        String email,
        int age
) {
    /**
     * Compact Constructor - 유효성 검증
     * record의 모든 필드를 자동으로 초기화하면서 검증 로직 추가
     */
    public User {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("나이는 0-150 사이여야 합니다");
        }
    }

    /**
     * ID 없이 생성하는 헬퍼 메서드 (신규 사용자용)
     */
    public static User createNew(String name, String email, int age) {
        return new User(null, name, email, age);
    }

    /**
     * ID를 할당하는 메서드 (저장 후 ID 부여용)
     */
    public User withId(Long id) {
        return new User(id, this.name, this.email, this.age);
    }
}