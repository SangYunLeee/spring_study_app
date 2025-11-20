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
 * QueryDSL (CommentRepositoryCustom):
 * - 타입 안전한 쿼리 작성
 * - 동적 쿼리 작성 쉬움
 * - Fetch Join으로 N+1 문제 해결
 * - 컴파일 타임 검증
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    /**
     * 게시글로 댓글 조회
     *
     * 주의: N+1 문제 발생 가능!
     * - Fetch Join이 필요하면 QueryDSL 메서드 사용
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
}
