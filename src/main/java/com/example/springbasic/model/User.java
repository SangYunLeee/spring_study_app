package com.example.springbasic.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
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
 *
 * 상속:
 * - BaseEntity: id, createdAt, updatedAt 공통 필드
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "age", nullable = false)
    private Integer age;

    // ========== 연관관계: Post (작성한 게시글 목록) ==========

    /**
     * @OneToMany: 일대다 관계
     * - 하나의 User가 여러 Post를 작성
     * - mappedBy = "author": Post 엔티티의 author 필드가 주인
     * - cascade = CascadeType.ALL: User 저장/삭제 시 Post도 함께 (실무에서는 주의!)
     * - orphanRemoval = true: User와 연관 끊긴 Post 자동 삭제 (실무에서는 주의!)
     * - fetch = FetchType.LAZY: 지연 로딩 (User 조회 시 Post는 조회 안함)
     *
     * 주의사항:
     * - cascade, orphanRemoval은 신중하게 사용
     * - 실무에서는 보통 User 삭제 시 Post는 유지하거나 soft delete 사용
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

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

    // ========== 비즈니스 메서드 ==========

    /**
     * 사용자 정보 수정 (전체)
     *
     * @param name 새 이름
     * @param email 새 이메일
     * @param age 새 나이
     */
    public void update(String name, String email, Integer age) {
        validateName(name);
        validateEmail(email);
        validateAge(age);
        this.name = name;
        this.email = email;
        this.age = age;
    }

    /**
     * 사용자 정보 수정 (부분)
     * null이 아닌 필드만 업데이트
     */
    public void update(UpdateRequest request) {
        if (request.name() != null) {
            validateName(request.name());
            this.name = request.name();
        }
        if (request.email() != null) {
            validateEmail(request.email());
            this.email = request.email();
        }
        if (request.age() != null) {
            validateAge(request.age());
            this.age = request.age();
        }
    }

    /**
     * 부분 업데이트를 위한 요청 DTO
     */
    public record UpdateRequest(String name, String email, Integer age) {
        public static UpdateRequest of(String name, String email, Integer age) {
            return new UpdateRequest(name, email, age);
        }
    }

    // ========== Getter ==========

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAge() {
        return age;
    }

    /**
     * 작성한 게시글 목록 조회
     * - 읽기 전용으로 반환 (외부에서 직접 수정 방지)
     */
    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }

    // ========== 연관관계 편의 메서드 ==========

    /**
     * 게시글 추가
     * - 양방향 연관관계 설정 (User ↔ Post)
     */
    public void addPost(Post post) {
        posts.add(post);
        post.setAuthor(this);  // 양방향 설정
    }

    /**
     * 게시글 제거
     * - orphanRemoval = true 덕분에 DB에서도 자동 삭제
     */
    public void removePost(Post post) {
        posts.remove(post);
        post.setAuthor(null);  // 연관관계 해제
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