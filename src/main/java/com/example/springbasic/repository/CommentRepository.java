package com.example.springbasic.repository;

import com.example.springbasic.model.Comment;
import com.example.springbasic.model.Post;
import com.example.springbasic.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment 엔티티 Repository
 *
 * Spring Data JPA:
 * - JpaRepository 상속으로 기본 CRUD 자동 제공
 * - 메서드 이름으로 쿼리 자동 생성
 * - @Query로 복잡한 쿼리 작성
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

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

    /**
     * 게시글로 댓글 조회 (Fetch Join - 작성자 포함)
     *
     * Fetch Join:
     * - Comment와 Author를 한 번의 쿼리로 조회
     * - N+1 문제 해결!
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdWithAuthor(@Param("postId") Long postId);

    /**
     * 게시글로 댓글 조회 (Fetch Join - 작성자 + 게시글 포함)
     *
     * 주의:
     * - 다중 Fetch Join
     * - author와 post를 모두 조회
     */
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.author " +
           "JOIN FETCH c.post " +
           "WHERE c.post.id = :postId " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdWithAuthorAndPost(@Param("postId") Long postId);

    /**
     * 최신 댓글 조회 (Fetch Join)
     */
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.author " +
           "JOIN FETCH c.post " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsWithDetails(Pageable pageable);
}
