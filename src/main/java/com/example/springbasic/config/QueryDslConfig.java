package com.example.springbasic.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정
 *
 * QueryDSL:
 * - 타입 안전한 쿼리 작성
 * - 컴파일 타임 검증
 * - IDE 자동완성 지원
 * - 동적 쿼리 작성 쉬움
 *
 * JPQL vs QueryDSL:
 * - JPQL: 문자열 기반 → 런타임 에러
 * - QueryDSL: Java 코드 → 컴파일 타임 에러
 *
 * 사용 예시:
 * <pre>
 * {@code
 * QPost post = QPost.post;
 * QUser user = QUser.user;
 *
 * List<Post> posts = queryFactory
 *     .selectFrom(post)
 *     .join(post.author, user).fetchJoin()
 *     .where(post.title.contains("검색어"))
 *     .orderBy(post.createdAt.desc())
 *     .fetch();
 * }
 * </pre>
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory Bean 등록
     *
     * JPAQueryFactory:
     * - QueryDSL 쿼리 생성의 시작점
     * - EntityManager를 주입받아 생성
     * - Repository에서 @Autowired로 주입받아 사용
     *
     * @return JPAQueryFactory 인스턴스
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
