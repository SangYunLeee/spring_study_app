package com.example.springbasic.repository;

import com.example.springbasic.model.Comment;
import com.example.springbasic.model.Post;
import com.example.springbasic.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment 엔티티 Repository
 *
 * Spring Data JPA:
 * - JpaRepository 상속으로 기본 CRUD 자동 제공
 * - 메서드 이름으로 쿼리 자동 생성
 *
 * QueryDSL:
 * - CommentRepositoryCustom 상속으로 QueryDSL 메서드 추가
 * - 타입 안전한 쿼리 작성
 * - 동적 쿼리 작성 쉬움
 * - JPQL @Query 대신 QueryDSL 사용
 *
 * 마이그레이션 완료:
 * - ❌ JPQL @Query → ✅ QueryDSL
 * - findByPostIdWithAuthor() → findByPostIdWithAuthorUsingQueryDsl()
 * - findByPostIdWithAuthorAndPost() → findByPostIdWithAuthorAndPostUsingQueryDsl()
 * - findRecentCommentsWithDetails() → findRecentCommentsWithDetailsUsingQueryDsl()
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    /**
     * 게시글로 댓글 조회
     * - N+1 문제 발생 가능!
     */
    List<Comment> findByPost(Post post);

    /**
     * 게시글 ID로 댓글 조회
     */
    List<Comment> findByPostId(Long postId);

    /**
     * 작성자로 댓글 조회
     */
    List<Comment> findByAuthor(User author);

    /**
     * 작성자 ID로 댓글 조회 (페이징)
     */
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

    /**
     * 게시글 ID로 댓글 개수 조회
     */
    long countByPostId(Long postId);

    // ========================================
    // 아래 JPQL 메서드들은 QueryDSL로 마이그레이션 완료
    // CommentRepositoryCustom에서 사용하세요
    // ========================================

    // ❌ 제거됨: @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId ...")
    // ✅ 대체: findByPostIdWithAuthorUsingQueryDsl(Long postId)

    // ❌ 제거됨: @Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.post ...")
    // ✅ 대체: findByPostIdWithAuthorAndPostUsingQueryDsl(Long postId)

    // ❌ 제거됨: @Query("SELECT c FROM Comment c JOIN FETCH c.author JOIN FETCH c.post ORDER BY ...")
    // ✅ 대체: findRecentCommentsWithDetailsUsingQueryDsl(Pageable pageable)
}
