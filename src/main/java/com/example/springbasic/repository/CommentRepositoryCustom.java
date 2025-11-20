package com.example.springbasic.repository;

import com.example.springbasic.model.Comment;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Comment Repository Custom 인터페이스
 *
 * QueryDSL을 사용한 커스텀 쿼리 메서드 정의
 *
 * Spring Data JPA 규칙:
 * - Custom 인터페이스 생성: CommentRepositoryCustom
 * - 구현 클래스 생성: CommentRepositoryImpl (이름 규칙 중요!)
 * - 기존 Repository가 Custom 인터페이스 상속
 *
 * 장점:
 * - 타입 안전한 쿼리
 * - 컴파일 타임 검증
 * - 동적 쿼리 작성 쉬움
 */
public interface CommentRepositoryCustom {

    /**
     * 게시글별 댓글 조회 (Fetch Join - Author + Post)
     *
     * Fetch Join으로 N+1 문제 해결:
     * - Comment, Author, Post를 1개 쿼리로 조회
     * - 문자열 오타 방지
     * - 필드명 변경 시 자동 추적
     * - IDE 자동완성
     */
    List<Comment> findByPostIdWithAuthorAndPostUsingQueryDsl(Long postId);

    /**
     * 게시글별 댓글 조회 (Fetch Join - Author만)
     *
     * Author만 Fetch Join (Post는 제외)
     * - 경량 쿼리 (필요한 것만 조회)
     */
    List<Comment> findByPostIdWithAuthorUsingQueryDsl(Long postId);

    /**
     * 최신 댓글 조회 (Fetch Join + Pageable)
     *
     * 페이징 지원:
     * - offset/limit으로 페이지 처리
     */
    List<Comment> findRecentCommentsWithDetailsUsingQueryDsl(Pageable pageable);

    /**
     * 동적 검색
     *
     * BooleanBuilder로 조건 동적 조합:
     * - content: 내용 검색 (LIKE)
     * - postId: 게시글 필터
     * - authorId: 작성자 필터
     * - null인 조건은 자동 제외
     */
    List<Comment> searchComments(String content, Long postId, Long authorId);
}
