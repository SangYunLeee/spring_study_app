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
     * QueryDSL: 게시글별 댓글 조회 (Fetch Join)
     *
     * 비교:
     * - JPQL: 문자열 기반, 런타임 검증
     * - QueryDSL: Java 코드, 컴파일 타임 검증
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
                .where(comment.post.id.eq(postId))       // WHERE 조건 (타입 안전!)
                .orderBy(comment.createdAt.asc())        // ORDER BY
                .fetch();                                 // 실행
    }

    /**
     * QueryDSL: 최신 댓글 조회 (Fetch Join + Pageable)
     *
     * Pageable 적용:
     * - offset(): 페이지 시작 위치
     * - limit(): 페이지 크기
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
                .offset(pageable.getOffset())     // 페이징: 시작 위치
                .limit(pageable.getPageSize())    // 페이징: 개수
                .fetch();
    }

    /**
     * QueryDSL: 동적 검색
     *
     * BooleanBuilder:
     * - 조건을 동적으로 추가
     * - null 체크 후 조건 추가
     * - 여러 조건 AND/OR 조합
     *
     * 예시:
     * - content가 null → 조건 추가 안함
     * - content가 "검색어" → LIKE 검색 추가
     */
    @Override
    public List<Comment> searchComments(String content, Long postId, Long authorId) {
        QComment comment = QComment.comment;
        QUser user = QUser.user;
        QPost post = QPost.post;

        // 동적 조건 빌더
        BooleanBuilder builder = new BooleanBuilder();

        // 내용 검색 (옵션)
        if (content != null && !content.isBlank()) {
            builder.and(comment.content.contains(content));  // LIKE '%검색어%'
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
                .where(builder)  // 동적 조건 적용
                .orderBy(comment.createdAt.desc())
                .fetch();
    }
}
