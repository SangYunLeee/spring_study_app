package com.example.springbasic.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 댓글 엔티티
 *
 * JPA 연관관계:
 * - User (N:1) - 작성자
 * - Post (N:1) - 게시글
 *
 * 학습 포인트:
 * - @ManyToOne: Comment → User, Comment → Post (다대일)
 * - FetchType.LAZY: 지연 로딩
 * - 연관관계의 주인: @JoinColumn이 있는 쪽
 */
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // ========== 연관관계: User (작성자) ==========

    /**
     * @ManyToOne: 다대일 관계
     * - 여러 Comment가 하나의 User에 속함
     * - fetch = FetchType.LAZY: 지연 로딩
     * - optional = false: User는 필수
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    // ========== 연관관계: Post (게시글) ==========

    /**
     * @ManyToOne: 다대일 관계
     * - 여러 Comment가 하나의 Post에 속함
     * - fetch = FetchType.LAZY: 지연 로딩
     * - optional = false: Post는 필수
     *
     * 연관관계의 주인:
     * - Comment 엔티티가 외래 키(post_id)를 관리
     * - Post 엔티티의 @OneToMany에서 mappedBy = "post"로 연결
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== JPA 필수: 기본 생성자 ==========

    protected Comment() {
        // JPA가 리플렉션으로 사용
    }

    // ========== 비즈니스 생성자 ==========

    /**
     * 댓글 생성 (정적 팩토리 메서드)
     */
    public static Comment create(String content, User author, Post post) {
        Comment comment = new Comment();
        comment.content = content;
        comment.author = author;
        comment.post = post;
        comment.validateContent(content);
        return comment;
    }

    // ========== 유효성 검증 ==========

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다");
        }
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 댓글 수정
     */
    public void update(String content) {
        validateContent(content);
        this.content = content;
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

    /**
     * ID 설정 (테스트 전용)
     */
    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        validateContent(content);
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
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
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content.substring(0, Math.min(content.length(), 50)) + "...'" +
                ", author=" + (author != null ? author.getName() : "null") +
                ", createdAt=" + createdAt +
                '}';
    }
}
