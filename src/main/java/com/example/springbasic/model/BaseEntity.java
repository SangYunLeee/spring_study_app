package com.example.springbasic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 필드를 담는 추상 클래스
 *
 * 공통 필드:
 * - id: 기본 키 (자동 생성)
 * - createdAt: 생성 일시
 * - updatedAt: 수정 일시
 *
 * JPA Auditing:
 * - @PrePersist: 엔티티 저장 전 자동 호출
 * - @PreUpdate: 엔티티 수정 전 자동 호출
 *
 * 상속 전략:
 * - @MappedSuperclass: 테이블로 생성되지 않음
 * - 자식 엔티티 테이블에 컬럼으로 포함됨
 *
 * 장점:
 * - 코드 중복 제거
 * - 일관된 타임스탬프 관리
 * - 유지보수 편리
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    protected LocalDateTime updatedAt;

    // ========== JPA 생명주기 콜백 ==========

    /**
     * 엔티티 저장 전 자동 호출
     * - createdAt, updatedAt 초기화
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티 수정 전 자동 호출
     * - updatedAt 갱신
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== Getter/Setter ==========

    public Long getId() {
        return id;
    }

    /**
     * ID 설정 (테스트 전용)
     * 실제 운영 코드에서는 사용하지 말 것 (DB가 자동 생성)
     */
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}