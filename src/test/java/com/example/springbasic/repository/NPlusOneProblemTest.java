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
 * N+1 문제 학습 테스트
 *
 * 학습 목표:
 * 1. N+1 문제가 무엇인지 이해
 * 2. 언제 발생하는지 확인
 * 3. Fetch Join으로 해결하는 방법 학습
 * 4. SQL 로그를 통해 쿼리 개수 확인
 *
 * 실행 방법:
 * - 테스트 실행 후 콘솔에서 Hibernate SQL 로그 확인
 * - "select" 키워드 개수 세기
 * - N+1 문제: N개의 추가 쿼리 발생
 * - Fetch Join: 1개의 쿼리만 발생
 */
@SpringBootTest
@DisplayName("N+1 문제 학습 테스트")
class NPlusOneProblemTest {

    // 로컬 PostgreSQL 사용 (docker-compose로 실행된 DB)
    // src/test/resources/application.yml의 설정 사용

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User user1;
    private User user2;
    private Post post1;
    private Post post2;
    private Post post3;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 정리
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // 사용자 생성
        user1 = userRepository.save(User.createNew("홍길동", "hong@example.com", 25));
        user2 = userRepository.save(User.createNew("김철수", "kim@example.com", 30));

        // 게시글 생성
        post1 = Post.create("첫 번째 게시글", "내용 1", user1);
        post2 = Post.create("두 번째 게시글", "내용 2", user1);
        post3 = Post.create("세 번째 게시글", "내용 3", user2);

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);

        // 댓글 생성
        commentRepository.save(Comment.create("댓글 1-1", user2, post1));
        commentRepository.save(Comment.create("댓글 1-2", user1, post1));
        commentRepository.save(Comment.create("댓글 2-1", user2, post2));
    }

    @Test
    @Transactional
    @DisplayName("N+1 문제 발생 예제 - findAll()")
    void nPlusOneProblem_WithFindAll() {
        System.out.println("\n========== N+1 문제 발생 시작 ==========");
        System.out.println("예상: 1(게시글 조회) + 3(각 게시글의 작성자 조회) = 4개 쿼리");

        // When: 모든 게시글 조회
        List<Post> posts = postRepository.findAll();

        System.out.println("\n========== Lazy Loading 시작 ==========");
        System.out.println("각 게시글의 작성자 이름을 출력할 때마다 추가 쿼리 발생!");

        // Then: 각 게시글의 작성자 이름 출력 (Lazy Loading 트리거)
        for (Post post : posts) {
            System.out.println("게시글: " + post.getTitle() + ", 작성자: " + post.getAuthor().getName());
            // ↑ post.getAuthor().getName() 호출 시마다 SELECT 쿼리 발생!
        }

        System.out.println("\n========== N+1 문제 종료 ==========\n");

        assertThat(posts).hasSize(3);
    }

    @Test
    @Transactional
    @DisplayName("N+1 문제 해결 - Fetch Join 사용")
    void nPlusOneProblem_Solved_WithFetchJoin() {
        System.out.println("\n========== Fetch Join 시작 ==========");
        System.out.println("예상: 1개의 JOIN 쿼리만 발생!");

        // When: Fetch Join으로 조회
        List<Post> posts = postRepository.findAllWithAuthor();

        System.out.println("\n========== 데이터 접근 ==========");
        System.out.println("이미 author가 로드되어 있어 추가 쿼리 없음!");

        // Then: 작성자 이름 출력 (추가 쿼리 없음!)
        for (Post post : posts) {
            System.out.println("게시글: " + post.getTitle() + ", 작성자: " + post.getAuthor().getName());
            // ↑ 추가 쿼리 발생 안함!
        }

        System.out.println("\n========== Fetch Join 종료 ==========\n");

        assertThat(posts).hasSize(3);
    }

    @Test
    @Transactional
    @DisplayName("N+1 문제 - 댓글 조회")
    void nPlusOneProblem_Comments() {
        System.out.println("\n========== 댓글 N+1 문제 ==========");

        // When: 게시글 ID로 댓글 조회
        List<Comment> comments = commentRepository.findByPostId(post1.getId());

        System.out.println("\n========== Lazy Loading ==========");

        // Then: 각 댓글의 작성자 이름 출력
        for (Comment comment : comments) {
            System.out.println("댓글: " + comment.getContent() + ", 작성자: " + comment.getAuthor().getName());
        }

        System.out.println("\n========== 종료 ==========\n");

        assertThat(comments).hasSize(2);
    }

    @Test
    @Transactional
    @DisplayName("N+1 문제 해결 - 댓글 Fetch Join (QueryDSL)")
    void nPlusOneProblem_Comments_Solved() {
        System.out.println("\n========== 댓글 Fetch Join (QueryDSL) ==========");

        // When: Fetch Join으로 댓글 + 작성자 조회 (QueryDSL 사용)
        List<Comment> comments = commentRepository.findByPostIdWithAuthorUsingQueryDsl(post1.getId());

        System.out.println("\n========== 데이터 접근 ==========");

        // Then: 작성자 이름 출력 (추가 쿼리 없음)
        for (Comment comment : comments) {
            System.out.println("댓글: " + comment.getContent() + ", 작성자: " + comment.getAuthor().getName());
        }

        System.out.println("\n========== 종료 ==========\n");

        assertThat(comments).hasSize(2);
    }

    @Test
    @Transactional
    @DisplayName("다중 Fetch Join - 게시글 + 작성자 + 댓글")
    void multipleFetchJoin() {
        System.out.println("\n========== 다중 Fetch Join ==========");
        System.out.println("게시글 + 작성자 + 모든 댓글을 1개 쿼리로 조회!");

        // When: 게시글 + 작성자 + 댓글 모두 조회
        Post post = postRepository.findByIdWithAuthorAndComments(post1.getId());

        System.out.println("\n========== 데이터 접근 ==========");

        // Then: 모든 데이터 출력 (추가 쿼리 없음)
        System.out.println("게시글: " + post.getTitle());
        System.out.println("작성자: " + post.getAuthor().getName());

        // 검증: Fetch Join으로 post와 author가 함께 조회됨
        assertThat(post).isNotNull();
        assertThat(post.getAuthor()).isNotNull();
        assertThat(post.getAuthor().getName()).isEqualTo("홍길동");

        System.out.println("\n========== 종료 ==========\n");
    }
}
