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
     * QueryDSL: 게시글별 댓글 조회 (Fetch Join)
     *
     * 기존 JPQL:
     * <pre>
     * @Query("SELECT c FROM Comment c " +
     *        "JOIN FETCH c.author " +
     *        "JOIN FETCH c.post " +
     *        "WHERE c.post.id = :postId " +
     *        "ORDER BY c.createdAt ASC")
     * </pre>
     *
     * QueryDSL 장점:
     * - 문자열 오타 방지
     * - 필드명 변경 시 자동 추적
     * - IDE 자동완성
     */
    List<Comment> findByPostIdWithAuthorAndPostUsingQueryDsl(Long postId);

    /**
     * QueryDSL: 최신 댓글 조회 (Fetch Join + Pageable)
     */
    List<Comment> findRecentCommentsWithDetailsUsingQueryDsl(Pageable pageable);

    /**
     * QueryDSL: 동적 검색 (내용 검색)
     *
     * 동적 쿼리 예시:
     * - content가 null이면 → 조건 제외
     * - content가 있으면 → LIKE 검색
     *
     * BooleanBuilder 사용
     */
    List<Comment> searchComments(String content, Long postId, Long authorId);
}
