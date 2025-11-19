package com.example.springbasic.exception;

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 *
 * 왜 필요한가?
 * - IllegalArgumentException보다 명확한 의미 전달
 * - 404 Not Found 상태 코드와 매핑하기 위함
 * - 비즈니스 로직에서 "찾을 수 없음"을 명시적으로 표현
 */
public class UserNotFoundException extends RuntimeException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. ID: " + userId);
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
