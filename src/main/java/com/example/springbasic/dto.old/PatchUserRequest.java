package com.example.springbasic.dto;

/**
 * 사용자 부분 수정 요청 DTO
 *
 * PATCH /api/users/{id} 요청 시 사용
 * 모든 필드가 선택적 (null 허용)
 */
public record PatchUserRequest(
        String name,    // null이면 수정하지 않음
        String email,   // null이면 수정하지 않음
        Integer age     // null이면 수정하지 않음 (int → Integer로 변경)
) {
    /**
     * 최소한의 검증만 수행
     * null이 아닌 필드만 검증
     */
    public PatchUserRequest {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("이름은 공백일 수 없습니다");
        }
        if (email != null && !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
        if (age != null && (age < 0 || age > 150)) {
            throw new IllegalArgumentException("나이는 0-150 사이여야 합니다");
        }
    }

    /**
     * 적어도 하나의 필드는 제공되어야 함
     */
    public boolean hasAtLeastOneField() {
        return name != null || email != null || age != null;
    }
}