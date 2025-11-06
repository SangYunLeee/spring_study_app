package com.example.springbasic.dto;

import com.example.springbasic.model.User;

/**
 * 사용자 응답 DTO
 *
 * API 응답 시 사용
 *
 * Entity를 직접 반환하지 않는 이유:
 * 1. 민감한 정보 제외 (비밀번호 등)
 * 2. 응답 형식 제어
 * 3. Entity 변경 시 API 영향 최소화
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        int age
) {
    /**
     * User 엔티티를 UserResponse로 변환하는 팩토리 메서드
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.name(),
                user.email(),
                user.age()
        );
    }

    /**
     * User 엔티티를 UserResponse로 변환 (인스턴스 메서드 버전)
     */
    public static UserResponse of(User user) {
        return from(user);
    }
}