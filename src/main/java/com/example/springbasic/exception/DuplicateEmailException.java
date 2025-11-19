package com.example.springbasic.exception;

/**
 * 이메일이 중복될 때 발생하는 예외
 *
 * 왜 필요한가?
 * - 중복 이메일은 "잘못된 요청" (400 Bad Request)으로 처리
 * - IllegalArgumentException보다 구체적
 * - 클라이언트가 어떤 필드가 중복인지 명확히 알 수 있음
 */
public class DuplicateEmailException extends RuntimeException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("이미 사용 중인 이메일입니다: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
