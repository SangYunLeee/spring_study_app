package com.example.springbasic.repository;

import com.example.springbasic.model.Post;
import com.example.springbasic.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Post 엔티티 Repository
 *
 * Spring Data JPA:
 * - JpaRepository 상속으로 기본 CRUD 자동 제공
 * - 메서드 이름으로 쿼리 자동 생성
 *
 * QueryDSL (PostRepositoryCustom):
 * - 타입 안전한 쿼리 작성
 * - 동적 쿼리 작성 쉬움
 * - Fetch Join으로 N+1 문제 해결
 * - 컴파일 타임 검증
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    /**
     * 작성자로 게시글 조회 (페이징)
     *
     * 주의: N+1 문제 발생 가능!
     * - Fetch Join이 필요하면 QueryDSL 메서드 사용
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
}
