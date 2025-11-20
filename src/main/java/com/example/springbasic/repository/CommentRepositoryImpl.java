package com.example.springbasic.repository;

import com.example.springbasic.model.Comment;
import com.example.springbasic.model.QComment;
import com.example.springbasic.model.QPost;
import com.example.springbasic.model.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment Repository Custom 구현체
 *
 * 중요:
 * - 클래스명은 반드시 "RepositoryImpl"로 끝나야 함
 * - Spring Data JPA가 자동으로 감지
 *
 * QueryDSL 사용 패턴:
 * 1. Q-Type 인스턴스 생성
 * 2. JPAQueryFactory 주입
 * 3. queryFactory로 쿼리 작성
 * 4. fetch() 또는 fetchOne()으로 실행
 */
@Repository
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자 주입 (권장)
     */
    public CommentRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 게시글별 댓글 조회 (Fetch Join - Author + Post)
     *
     * N+1 문제 해결:
     * - 1개 쿼리로 Comment + Author + Post 조회
     * - 타입 안전: 컴파일 타임 검증
     */
    @Override
    public List<Comment> findByPostIdWithAuthorAndPostUsingQueryDsl(Long postId) {
        QComment comment = QComment.comment;
        QUser user = QUser.user;
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(comment)
                .join(comment.author, user).fetchJoin()  // Fetch Join
                .join(comment.post, post).fetchJoin()    // Fetch Join
                .where(comment.post.id.eq(postId))       // WHERE 조건
                .orderBy(comment.createdAt.asc())        // ORDER BY
                .fetch();
    }

    /**
     * 게시글별 댓글 조회 (Fetch Join - Author만)
     *
     * 경량 쿼리:
     * - Post는 Fetch Join 안 함
     * - Author만 필요한 경우 사용
     */
    @Override
    public List<Comment> findByPostIdWithAuthorUsingQueryDsl(Long postId) {
        QComment comment = QComment.comment;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(comment)
                .join(comment.author, user).fetchJoin()  // Author만 Fetch Join
                .where(comment.post.id.eq(postId))       // WHERE 조건
                .orderBy(comment.createdAt.asc())        // ORDER BY
                .fetch();
    }

    /**
     * 최신 댓글 조회 (Fetch Join + Pageable)
     *
     * 페이징 지원:
     * - offset(): 시작 위치
     * - limit(): 개수
     */
    @Override
    public List<Comment> findRecentCommentsWithDetailsUsingQueryDsl(Pageable pageable) {
        QComment comment = QComment.comment;
        QUser user = QUser.user;
        QPost post = QPost.post;

        return queryFactory
                .selectFrom(comment)
                .join(comment.author, user).fetchJoin()
                .join(comment.post, post).fetchJoin()
                .orderBy(comment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    /**
     * 동적 검색
     *
     * BooleanBuilder로 조건 동적 조합:
     * - null인 파라미터는 조건에서 제외
     * - 여러 조건을 AND로 결합
     */
    @Override
    public List<Comment> searchComments(String content, Long postId, Long authorId) {
        QComment comment = QComment.comment;
        QUser user = QUser.user;
        QPost post = QPost.post;

        BooleanBuilder builder = new BooleanBuilder();

        // 내용 검색 (옵션)
        if (content != null && !content.isBlank()) {
            builder.and(comment.content.contains(content));
        }

        // 게시글 ID 필터 (옵션)
        if (postId != null) {
            builder.and(comment.post.id.eq(postId));
        }

        // 작성자 ID 필터 (옵션)
        if (authorId != null) {
            builder.and(comment.author.id.eq(authorId));
        }

        return queryFactory
                .selectFrom(comment)
                .join(comment.author, user).fetchJoin()
                .join(comment.post, post).fetchJoin()
                .where(builder)
                .orderBy(comment.createdAt.desc())
                .fetch();
    }
}
