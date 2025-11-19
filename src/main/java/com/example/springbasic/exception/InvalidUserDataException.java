package com.example.springbasic.exception;

/**
 * 사용자 데이터가 유효하지 않을 때 발생하는 예외
 *
 * 예시:
 * - 나이가 음수
 * - 이름이 너무 길거나 짧음
 * - 필수 필드 누락
 *
 * 왜 필요한가?
 * - 유효성 검증 실패를 명확히 표현
 * - 400 Bad Request와 매핑
 */
public class InvalidUserDataException extends RuntimeException {

    private final String field;

    public InvalidUserDataException(String message) {
        super(message);
        this.field = null;
    }

    public InvalidUserDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
