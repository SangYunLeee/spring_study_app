package com.example.springbasic.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 사용자 정보를 담는 JPA Entity + Spring Security UserDetails
 *
 * DB 명세와 매핑:
 * - dbmate 마이그레이션에 정의된 테이블 구조
 * - 테이블명: users
 * - 컬럼: id, name, email, age, password, role, created_at, updated_at
 *
 * 계층 분리:
 * - 도메인 모델 (Service, Repository에서 사용)
 * - API 모델과 분리 (Controller에서 변환)
 *
 * Spring Security 통합:
 * - UserDetails 인터페이스 구현 (인증/인가)
 *
 * 상속:
 * - BaseEntity: id, createdAt, updatedAt 공통 필드
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "age", nullable = false)
    private Integer age;

    /**
     * 비밀번호 (BCrypt 암호화)
     * - 최소 8자 이상 권장
     * - BCryptPasswordEncoder로 암호화하여 저장
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 사용자 권한
     * - USER: 일반 사용자
     * - ADMIN: 관리자
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;

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
     * 사용자 생성 (정적 팩토리 메서드)
     * 검증은 Controller에서 완료됨 (OpenAPI 스키마 기반)
     */
    public static User createNew(CreateRequest request) {
        User user = new User();
        user.name = request.name();
        user.email = request.email();
        user.age = request.age();
        return user;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 사용자 정보 수정
     * null이 아닌 필드만 업데이트
     * 검증은 Controller에서 완료됨 (OpenAPI 스키마 기반)
     */
    public void update(UpdateRequest request) {
        if (request.name() != null) {
            this.name = request.name();
        }
        if (request.email() != null) {
            this.email = request.email();
        }
        if (request.age() != null) {
            this.age = request.age();
        }
        if (request.password() != null) {
            this.password = request.password();
        }
        if (request.role() != null) {
            this.role = request.role();
        }
    }

    /**
     * 사용자 생성 요청 DTO
     * 순수 데이터 전달 객체 (검증은 Controller에서 수행)
     */
    public record CreateRequest(
            String name,
            String email,
            Integer age
    ) {
        public static CreateRequest of(String name, String email, Integer age) {
            return new CreateRequest(name, email, age);
        }
    }

    /**
     * 사용자 수정 요청 DTO
     * null 허용 (부분 업데이트)
     * 순수 데이터 전달 객체 (검증은 Controller에서 수행)
     */
    public record UpdateRequest(
            String name,
            String email,
            Integer age,
            String password,  // BCrypt 암호화된 비밀번호
            Role role
    ) {
        public static UpdateRequest of(String name, String email, Integer age, String password, Role role) {
            return new UpdateRequest(name, email, age, password, role);
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

    public Role getRole() {
        return role;
    }

    /**
     * 작성한 게시글 목록 조회
     * - 읽기 전용으로 반환 (외부에서 직접 수정 방지)
     */
    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }

    // ========== UserDetails 구현 (Spring Security) ==========

    /**
     * 사용자의 권한 목록 반환
     * - Spring Security가 인가(Authorization)에 사용
     * - Role enum의 authority를 GrantedAuthority로 변환
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }

    /**
     * 비밀번호 반환
     * - BCrypt로 암호화된 값
     * - Spring Security가 인증(Authentication)에 사용
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 사용자명 반환
     * - 이 프로젝트에서는 email을 username으로 사용
     * - Spring Security가 인증에 사용
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * 계정 만료 여부
     * - true: 계정이 만료되지 않음
     * - false: 계정이 만료됨 (로그인 불가)
     *
     * 현재는 항상 true (만료 기능 미사용)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부
     * - true: 계정이 잠기지 않음
     * - false: 계정이 잠김 (로그인 불가)
     *
     * 현재는 항상 true (잠금 기능 미사용)
     * 실무에서는 로그인 실패 횟수 제한 등에 활용
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호 만료 여부
     * - true: 비밀번호가 만료되지 않음
     * - false: 비밀번호가 만료됨 (변경 필요)
     *
     * 현재는 항상 true (만료 기능 미사용)
     * 실무에서는 90일마다 비밀번호 변경 등에 활용
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부
     * - true: 계정이 활성화됨
     * - false: 계정이 비활성화됨 (로그인 불가)
     *
     * 현재는 항상 true (활성화 상태)
     * 실무에서는 이메일 인증, 관리자 승인 등에 활용
     */
    @Override
    public boolean isEnabled() {
        return true;
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