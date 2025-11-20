package com.example.springbasic.repository;

import com.example.springbasic.model.Post;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Post Repository Custom 인터페이스
 *
 * QueryDSL을 사용한 커스텀 쿼리 메서드 정의
 *
 * Spring Data JPA 규칙:
 * - Custom 인터페이스 생성: PostRepositoryCustom
 * - 구현 클래스 생성: PostRepositoryImpl (이름 규칙 중요!)
 * - 기존 Repository가 Custom 인터페이스 상속
 *
 * 장점:
 * - 타입 안전한 쿼리
 * - 컴파일 타임 검증
 * - 동적 쿼리 작성 쉬움
 */
public interface PostRepositoryCustom {

    /**
     * 작성자별 게시글 조회 (Fetch Join - Author)
     *
     * Fetch Join으로 N+1 문제 해결:
     * - Post, Author를 1개 쿼리로 조회
     */
    List<Post> findByAuthorIdWithAuthorUsingQueryDsl(Long authorId);

    /**
     * 모든 게시글 조회 (Fetch Join - Author)
     *
     * 모든 게시글과 작성자를 함께 조회
     */
    List<Post> findAllWithAuthorUsingQueryDsl();

    /**
     * ID로 게시글 조회 (Fetch Join - Author)
     *
     * 단건 조회 시에도 작성자 함께 조회
     */
    Post findByIdWithAuthorUsingQueryDsl(Long postId);

    /**
     * ID로 게시글 조회 (Fetch Join - Author + Comments)
     *
     * 게시글, 작성자, 댓글을 모두 조회
     * DISTINCT로 중복 제거
     */
    Post findByIdWithAuthorAndCommentsUsingQueryDsl(Long postId);

    /**
     * 최신 게시글 조회 (Fetch Join - Author + Pageable)
     *
     * 페이징 지원:
     * - offset/limit으로 페이지 처리
     * - 생성일 기준 내림차순 정렬
     */
    List<Post> findRecentPostsWithAuthorUsingQueryDsl(Pageable pageable);
}