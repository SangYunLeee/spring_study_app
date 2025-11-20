package com.example.springbasic.model;

import jakarta.persistence.*;
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
 *
 * 상속:
 * - BaseEntity: id, createdAt, updatedAt 공통 필드
 */
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

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

    // ========== JPA 필수: 기본 생성자 ==========

    protected Comment() {
        // JPA가 리플렉션으로 사용
    }

    // ========== 비즈니스 생성자 ==========

    /**
     * 댓글 생성 (정적 팩토리 메서드)
     * 검증은 Controller에서 완료됨 (OpenAPI 스키마 기반)
     */
    public static Comment create(CreateRequest request, User author, Post post) {
        Comment comment = new Comment();
        comment.content = request.content();
        comment.author = author;
        comment.post = post;
        return comment;
    }

    // ========== 비즈니스 메서드 ==========

    /**
     * 댓글 수정
     * null이 아닌 필드만 업데이트
     * 검증은 Controller에서 완료됨 (OpenAPI 스키마 기반)
     */
    public void update(UpdateRequest request) {
        if (request.content() != null) {
            this.content = request.content();
        }
    }

    /**
     * 댓글 생성 요청 DTO
     * 순수 데이터 전달 객체 (검증은 Controller에서 수행)
     */
    public record CreateRequest(
            String content
    ) {
        public static CreateRequest of(String content) {
            return new CreateRequest(content);
        }
    }

    /**
     * 댓글 수정 요청 DTO
     * null 허용 (부분 업데이트)
     * 순수 데이터 전달 객체 (검증은 Controller에서 수행)
     */
    public record UpdateRequest(
            String content
    ) {
        public static UpdateRequest of(String content) {
            return new UpdateRequest(content);
        }
    }

    // ========== Getter ==========

    public String getContent() {
        return content;
    }

    public User getAuthor() {
        return author;
    }

    public Post getPost() {
        return post;
    }

    /**
     * 연관관계 설정용 (package-private)
     * - 외부에서 직접 호출 금지
     * - addComment/removeComment 메서드를 통해서만 사용
     */
    void setAuthor(User author) {
        this.author = author;
    }

    /**
     * 연관관계 설정용 (package-private)
     * - 외부에서 직접 호출 금지
     * - addComment/removeComment 메서드를 통해서만 사용
     */
    void setPost(Post post) {
        this.post = post;
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
