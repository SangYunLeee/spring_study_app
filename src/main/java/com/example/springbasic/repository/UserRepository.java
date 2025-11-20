package com.example.springbasic.repository;

import com.example.springbasic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 데이터를 저장하고 조회하는 Repository (JPA 기반)
 *
 * JpaRepository를 상속받아 기본 CRUD 메서드 자동 제공:
 * - save(User) : 저장/수정
 * - findById(Long) : ID로 조회
 * - findAll() : 전체 조회
 * - deleteById(Long) : 삭제
 * - count() : 개수
 * - existsById(Long) : 존재 여부
 *
 * DB 명세와 연결:
 * - dbmate 마이그레이션으로 생성된 users 테이블
 * - User Entity(@Entity)가 테이블과 매핑됨
 *
 * Spring Data JPA 쿼리 메서드:
 * - 메서드 이름으로 쿼리 자동 생성
 * - findByEmail → SELECT * FROM users WHERE email = ?
 * - findByNameContaining → SELECT * FROM users WHERE name LIKE %?%
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 찾기
     *
     * 쿼리 메서드 명명 규칙:
     * - findBy + 필드명 → WHERE 절 생성
     * - email 필드로 검색
     *
     * 생성되는 SQL:
     * SELECT * FROM users WHERE email = ?
     *
     * 인덱스 활용:
     * - 20250101000002_add_email_index.sql에서 생성한 인덱스 사용
     * - 빠른 검색 성능
     */
    Optional<User> findByEmail(String email);

    /**
     * 이름으로 사용자 검색 (부분 일치)
     *
     * 쿼리 메서드 명명 규칙:
     * - Containing → LIKE %keyword% 검색
     *
     * 생성되는 SQL:
     * SELECT * FROM users WHERE name LIKE %?%
     *
     * 참고:
     * - 부분 검색이므로 인덱스 효율 낮음
     * - 데이터가 많아지면 Full-text search 고려
     */
    List<User> findByNameContaining(String keyword);

    // JpaRepository에서 상속받는 기본 메서드들:
    //
    // User save(User user)
    //   - INSERT 또는 UPDATE (ID 유무로 자동 판단)
    //
    // Optional<User> findById(Long id)
    //   - SELECT * FROM users WHERE id = ?
    //
    // List<User> findAll()
    //   - SELECT * FROM users
    //
    // void deleteById(Long id)
    //   - DELETE FROM users WHERE id = ?
    //
    // long count()
    //   - SELECT COUNT(*) FROM users
    //
    // boolean existsById(Long id)
    //   - SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)
}