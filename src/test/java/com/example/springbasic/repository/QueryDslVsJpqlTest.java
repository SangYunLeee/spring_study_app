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
 * QueryDSL vs JPQL 비교 테스트
 *
 * 학습 목표:
 * 1. JPQL과 QueryDSL의 차이 이해
 * 2. QueryDSL의 타입 안전성 경험
 * 3. 동적 쿼리 작성의 편리함 확인
 */
@SpringBootTest
@DisplayName("QueryDSL vs JPQL 비교 테스트")
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
    @DisplayName("JPQL vs QueryDSL - Fetch Join 비교")
    void compareJpqlAndQueryDsl() {
        System.out.println("\n========== JPQL 방식 ==========");
        List<Comment> jpqlComments = commentRepository.findByPostIdWithAuthorAndPost(post1.getId());
        System.out.println("JPQL 결과 개수: " + jpqlComments.size());

        System.out.println("\n========== QueryDSL 방식 ==========");
        List<Comment> querydslComments = commentRepository
                .findByPostIdWithAuthorAndPostUsingQueryDsl(post1.getId());
        System.out.println("QueryDSL 결과 개수: " + querydslComments.size());

        assertThat(jpqlComments).hasSize(3);
        assertThat(querydslComments).hasSize(3);
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
