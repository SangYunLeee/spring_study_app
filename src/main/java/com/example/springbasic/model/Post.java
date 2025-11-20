package com.example.springbasic.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 게시글 엔티티
 *
 * JPA 연관관계:
 * - User (N:1) - 작성자
 * - Comment (1:N) - 댓글 목록
 *
 * 학습 포인트:
 * - @ManyToOne: Post → User (다대일)
 * - @OneToMany: Post → Comment (일대다)
 * - FetchType.LAZY: 지연 로딩 (성능 최적화)
 * - cascade: 영속성 전이
 * - orphanRemoval: 고아 객체 제거
 *
 * 상속:
 * - BaseEntity: id, createdAt, updatedAt 공통 필드
 */
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // ========== 연관관계: User (작성자) ==========

    /**
     * @ManyToOne: 다대일 관계
     * - 여러 Post가 하나의 User에 속함
     * - fetch = FetchType.LAZY: 지연 로딩 (Post 조회 시 User는 프록시 객체)
     * - optional = false: User는 필수 (NULL 불가)
     *
     * @JoinColumn: 외래 키 설정
     * - name = "user_id": DB 컬럼명
     * - nullable = false: NOT NULL 제약
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    // ========== 연관관계: Comment (댓글 목록) ==========

    /**
     * @OneToMany: 일대다 관계
     * - 하나의 Post가 여러 Comment를 가짐
     * - mappedBy = "post": Comment 엔티티의 post 필드가 주인
     * - cascade = CascadeType.ALL: Post 저장/삭제 시 Comment도 함께
     * - orphanRemoval = true: 연관관계가 끊긴 Comment 자동 삭제
     * - fetch = FetchType.LAZY: 지연 로딩 (필요할 때만 조회)
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    // ========== JPA 필수: 기본 생성자 ==========

    protected Post() {
        // JPA가 리플렉션으로 사용
    }

    // ========== 비즈니스 생성자 ==========

    /**
     * 게시글 생성 (정적 팩토리 메서드)
     */
    public static Post create(String title, String content, User author) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.author = author;
        post.validateTitle(title);
        post.validateContent(content);
        return post;
    }

    // ========== 유효성 검증 ==========

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("제목은 200자 이하여야 합니다");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다");
        }
    }

    // ========== 연관관계 편의 메서드 ==========

    /**
     * 댓글 추가
     * - 양방향 연관관계 설정 (Post ↔ Comment)
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);  // 양방향 설정
    }

    /**
     * 댓글 제거
     * - orphanRemoval = true 덕분에 DB에서도 자동 삭제
     */
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);  // 연관관계 해제
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 게시글 수정
     * null이 아닌 필드만 업데이트
     */
    public void update(UpdateRequest request) {
        if (request.title() != null) {
            validateTitle(request.title());
            this.title = request.title();
        }
        if (request.content() != null) {
            validateContent(request.content());
            this.content = request.content();
        }
    }

    /**
     * 부분 업데이트를 위한 요청 DTO
     */
    public record UpdateRequest(String title, String content) {
        public static UpdateRequest of(String title, String content) {
            return new UpdateRequest(title, content);
        }
    }

    // ========== Getter ==========

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public User getAuthor() {
        return author;
    }

    /**
     * 연관관계 설정용 (package-private)
     * - 외부에서 직접 호출 금지
     * - addPost/removePost 메서드를 통해서만 사용
     */
    void setAuthor(User author) {
        this.author = author;
    }

    /**
     * 댓글 목록 조회
     * - 읽기 전용으로 반환 (외부에서 직접 수정 방지)
     */
    public List<Comment> getComments() {
        return new ArrayList<>(comments);
    }

    // ========== Object 메서드 ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author=" + (author != null ? author.getName() : "null") +
                ", commentsCount=" + comments.size() +
                ", createdAt=" + createdAt +
                '}';
    }
}
