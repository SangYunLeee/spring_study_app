package com.example.springbasic.repository;

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
 * Post 엔티티 Repository
 *
 * Spring Data JPA:
 * - JpaRepository 상속으로 기본 CRUD 자동 제공
 * - 메서드 이름으로 쿼리 자동 생성
 * - @Query로 복잡한 쿼리 작성
 *
 * N+1 문제 학습:
 * - findAll() vs findAllWithAuthor()
 * - Lazy Loading의 장단점
 * - Fetch Join 사용법
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 작성자로 게시글 조회 (페이징)
     * - N+1 문제 발생 가능!
     * - Post 조회 후, 각 Post의 author 접근 시 추가 쿼리 발생
     */
    Page<Post> findByAuthor(User author, Pageable pageable);

    /**
     * 작성자 ID로 게시글 조회
     */
    List<Post> findByAuthorId(Long authorId);

    /**
     * 제목으로 검색 (LIKE 쿼리)
     */
    List<Post> findByTitleContaining(String title);

    /**
     * 작성자로 게시글 조회 (Fetch Join)
     *
     * Fetch Join:
     * - Post와 Author를 한 번의 쿼리로 조회
     * - N+1 문제 해결!
     * - JPQL의 "JOIN FETCH" 사용
     *
     * SQL:
     * SELECT p.*, u.*
     * FROM posts p
     * INNER JOIN users u ON p.user_id = u.id
     * WHERE u.id = :authorId
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.author.id = :authorId")
    List<Post> findByAuthorIdWithAuthor(@Param("authorId") Long authorId);

    /**
     * 모든 게시글 조회 (Fetch Join)
     *
     * 주의:
     * - Fetch Join과 Pageable은 함께 사용 주의!
     * - 메모리에서 페이징 처리됨 (경고 발생)
     * - 데이터가 많으면 성능 문제 가능
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author")
    List<Post> findAllWithAuthor();

    /**
     * 게시글 + 작성자 + 댓글 개수 조회
     *
     * LEFT JOIN:
     * - 댓글이 없는 게시글도 조회
     * - COUNT로 댓글 개수 계산
     */
    @Query("SELECT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "WHERE p.id = :postId")
    Post findByIdWithAuthor(@Param("postId") Long postId);

    /**
     * 게시글 + 작성자 + 모든 댓글 조회 (Fetch Join)
     *
     * 주의:
     * - 컬렉션 Fetch Join
     * - 1:N 관계에서 데이터 중복 발생 가능
     * - DISTINCT 사용 필요
     */
    @Query("SELECT DISTINCT p FROM Post p " +
           "JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.comments " +
           "WHERE p.id = :postId")
    Post findByIdWithAuthorAndComments(@Param("postId") Long postId);

    /**
     * 최신 게시글 조회 (Fetch Join)
     */
    @Query("SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.createdAt DESC")
    List<Post> findRecentPostsWithAuthor(Pageable pageable);
}
