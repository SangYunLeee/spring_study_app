package com.example.springbasic.repository;

import com.example.springbasic.model.Comment;
import com.example.springbasic.model.Post;
import com.example.springbasic.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QueryDSL 기능 테스트
 *
 * 학습 목표:
 * 1. QueryDSL의 타입 안전성 경험
 * 2. Fetch Join 구현 확인
 * 3. 동적 쿼리 작성의 편리함 확인
 *
 * 마이그레이션 완료:
 * - JPQL @Query 메서드 제거
 * - QueryDSL로 완전히 대체
 */
@SpringBootTest
@DisplayName("QueryDSL 기능 테스트")
class QueryDslVsJpqlTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User user1;
    private User user2;
    private Post post1;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        user1 = userRepository.save(User.createNew("홍길동", "hong@example.com", 25));
        user2 = userRepository.save(User.createNew("김철수", "kim@example.com", 30));

        post1 = Post.create("첫 번째 게시글", "내용 1", user1);
        postRepository.save(post1);

        commentRepository.save(Comment.create("좋은 글이네요!", user2, post1));
        commentRepository.save(Comment.create("감사합니다", user1, post1));
        commentRepository.save(Comment.create("동의합니다", user2, post1));
    }

    @Test
    @Transactional
    @DisplayName("QueryDSL - Fetch Join (Author + Post)")
    void queryDsl_FetchJoin() {
        System.out.println("\n========== QueryDSL Fetch Join ==========");

        // When: QueryDSL로 댓글 + 작성자 + 게시글 조회
        List<Comment> comments = commentRepository
                .findByPostIdWithAuthorAndPostUsingQueryDsl(post1.getId());

        System.out.println("QueryDSL 결과 개수: " + comments.size());

        // Then: 1개의 쿼리로 모든 데이터 조회 확인
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getAuthor()).isNotNull();
        assertThat(comments.get(0).getPost()).isNotNull();
    }

    @Test
    @Transactional
    @DisplayName("QueryDSL - Fetch Join (Author만)")
    void queryDsl_FetchJoinAuthorOnly() {
        System.out.println("\n========== QueryDSL Fetch Join (Author만) ==========");

        // When: QueryDSL로 댓글 + 작성자만 조회
        List<Comment> comments = commentRepository
                .findByPostIdWithAuthorUsingQueryDsl(post1.getId());

        System.out.println("QueryDSL 결과 개수: " + comments.size());

        // Then: 작성자만 Fetch Join 확인
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getAuthor()).isNotNull();
    }

    @Test
    @Transactional
    @DisplayName("QueryDSL - 동적 검색")
    void queryDsl_DynamicQuery() {
        System.out.println("\n========== 동적 검색 ==========");

        // 조건 없음
        List<Comment> all = commentRepository.searchComments(null, null, null);
        assertThat(all).hasSize(3);

        // 내용 검색
        List<Comment> byContent = commentRepository.searchComments("감사", null, null);
        assertThat(byContent).hasSize(1);

        // 작성자 필터
        List<Comment> byAuthor = commentRepository.searchComments(null, null, user2.getId());
        assertThat(byAuthor).hasSize(2);

        // 복합 조건
        List<Comment> combined = commentRepository.searchComments("좋은", null, user2.getId());
        assertThat(combined).hasSize(1);

        System.out.println("동적 검색 완료!");
    }
}
