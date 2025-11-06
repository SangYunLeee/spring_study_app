package com.example.springbasic.dto;

/**
 * 사용자 수정 요청 DTO
 *
 * PUT /api/users/{id} 요청 시 사용
 * 전체 필드를 모두 포함 (전체 수정)
 */
public record UpdateUserRequest(
        String name,
        String email,
        int age
) {
    public UpdateUserRequest {
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
}