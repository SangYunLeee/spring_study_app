package com.example.springbasic.dto;

/**
 * 사용자 생성 요청 DTO (Data Transfer Object)
 *
 * POST /api/users 요청 시 사용
 *
 * DTO를 사용하는 이유:
 * 1. Entity 직접 노출 방지 (보안)
 * 2. 필요한 필드만 정의 (id는 서버에서 생성)
 * 3. API 변경 시 Entity 영향 최소화
 * 4. 유효성 검증 규칙 분리
 */
public record CreateUserRequest(
        String name,
        String email,
        int age
) {
    /**
     * Compact Constructor - 간단한 유효성 검증
     */
    public CreateUserRequest {
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