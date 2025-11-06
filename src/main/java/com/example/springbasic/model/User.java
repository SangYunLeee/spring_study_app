package com.example.springbasic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 사용자 정보를 담는 JPA Entity
 *
 * DB 명세와 매핑:
 * - Liquibase 명세(001-create-users-table.yaml)에 정의된 테이블 구조
 * - 테이블명: users
 * - 컬럼: id, name, email, age, created_at, updated_at
 *
 * 계층 분리:
 * - 도메인 모델 (Service, Repository에서 사용)
 * - API 모델과 분리 (Controller에서 변환)
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== JPA 필수: 기본 생성자 ==========
    protected User() {
        // JPA가 리플렉션으로 사용
    }

    // ========== 비즈니스 생성자 ==========

    /**
     * 전체 필드 생성자 (기존 사용자 로딩 시)
     */
    public User(Long id, String name, String email, Integer age) {
        validateName(name);
        validateEmail(email);
        validateAge(age);

        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    /**
     * ID 없이 생성 (신규 사용자 생성 시)
     */
    public static User createNew(String name, String email, int age) {
        User user = new User();
        user.name = name;
        user.email = email;
        user.age = age;

        // 검증
        user.validateName(name);
        user.validateEmail(email);
        user.validateAge(age);

        return user;
    }

    // ========== 유효성 검증 ==========

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다");
        }
    }

    private void validateAge(Integer age) {
        if (age == null || age < 0 || age > 150) {
            throw new IllegalArgumentException("나이는 0-150 사이여야 합니다");
        }
    }

    // ========== JPA 생명주기 콜백 ==========

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== Getter/Setter ==========

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        validateAge(age);
        this.age = age;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ========== Object 메서드 ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}