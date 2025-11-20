package com.example.springbasic.model;

/**
 * 사용자 권한 Enum
 *
 * Spring Security의 권한 체계와 통합
 * - ROLE_ 접두사는 Spring Security 관례
 */
public enum Role {
    /**
     * 일반 사용자
     * - 기본 권한
     */
    USER("ROLE_USER"),

    /**
     * 관리자
     * - 모든 권한
     */
    ADMIN("ROLE_ADMIN");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    /**
     * Spring Security에서 사용할 권한 문자열
     * @return ROLE_USER 또는 ROLE_ADMIN
     */
    public String getAuthority() {
        return authority;
    }
}