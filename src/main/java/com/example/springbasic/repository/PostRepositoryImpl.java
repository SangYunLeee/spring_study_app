package com.example.springbasic.repository;

import com.example.springbasic.model.Post;
import com.example.springbasic.model.QComment;
import com.example.springbasic.model.QPost;
import com.example.springbasic.model.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Post Repository Custom 구현체
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
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 생성자 주입 (권장)
     */
    public PostRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 작성자별 게시글 조회 (Fetch Join - Author)
     *
     * N+1 문제 해결:
     * - 1개 쿼리로 Post + Author 조회
     * - 타입 안전: 컴파일 타임 검증
     */
    @Override
    public List<Post> findByAuthorIdWithAuthorUsingQueryDsl(Long authorId) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(post)
                .join(post.author, user).fetchJoin()  // Fetch Join
                .where(post.author.id.eq(authorId))   // WHERE 조건
                .fetch();
    }

    /**
     * 모든 게시글 조회 (Fetch Join - Author)
     *
     * 모든 게시글과 작성자를 함께 조회
     */
    @Override
    public List<Post> findAllWithAuthorUsingQueryDsl() {
        QPost post = QPost.post;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(post)
                .join(post.author, user).fetchJoin()  // Fetch Join
                .fetch();
    }

    /**
     * ID로 게시글 조회 (Fetch Join - Author)
     *
     * 단건 조회:
     * - fetchOne(): 단일 결과 반환
     * - 결과 없으면 null
     */
    @Override
    public Post findByIdWithAuthorUsingQueryDsl(Long postId) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(post)
                .leftJoin(post.author, user).fetchJoin()  // LEFT JOIN Fetch
                .where(post.id.eq(postId))                // WHERE 조건
                .fetchOne();
    }

    /**
     * ID로 게시글 조회 (Fetch Join - Author + Comments)
     *
     * DISTINCT 사용:
     * - 일대다 조인 시 중복 제거
     * - Post 1개에 Comment N개 → DISTINCT로 Post 1개만 반환
     */
    @Override
    public Post findByIdWithAuthorAndCommentsUsingQueryDsl(Long postId) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        QComment comment = QComment.comment;

        return queryFactory
                .selectFrom(post).distinct()              // DISTINCT
                .join(post.author, user).fetchJoin()      // Fetch Join - Author
                .leftJoin(post.comments, comment).fetchJoin()  // Fetch Join - Comments
                .where(post.id.eq(postId))                // WHERE 조건
                .fetchOne();
    }

    /**
     * 최신 게시글 조회 (Fetch Join - Author + Pageable)
     *
     * 페이징 지원:
     * - offset(): 시작 위치
     * - limit(): 개수
     * - orderBy(): 정렬
     */
    @Override
    public List<Post> findRecentPostsWithAuthorUsingQueryDsl(Pageable pageable) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(post)
                .join(post.author, user).fetchJoin()
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}