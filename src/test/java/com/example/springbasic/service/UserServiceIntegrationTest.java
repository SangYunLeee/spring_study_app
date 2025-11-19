package com.example.springbasic.service;

import com.example.springbasic.exception.DuplicateEmailException;
import com.example.springbasic.exception.UserNotFoundException;
import com.example.springbasic.model.User;
import com.example.springbasic.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserService 통합 테스트 (Integration Test)
 *
 * 통합 테스트란?
 * - 실제 Spring 애플리케이션 컨텍스트를 띄워서 테스트
 * - 실제 DB(PostgreSQL)와 연결하여 전체 흐름 테스트
 * - Mock을 사용하지 않고 실제 컴포넌트 사용
 *
 * @SpringBootTest:
 * - 전체 Spring 애플리케이션 컨텍스트 로드
 * - 실제 Bean들이 생성되고 의존성 주입됨
 * - 실제 DB 연결 설정 사용
 *
 * @TestPropertySource:
 * - 테스트 전용 설정 (필요 시)
 *
 * 장점:
 * - 실제 환경과 동일한 테스트
 * - DB 제약조건, 트랜잭션 등 모두 검증
 *
 * 단점:
 * - 느림 (Spring 컨텍스트 로딩 + 실제 DB 접근)
 * - 테스트 간 데이터 격리 필요
 */
@SpringBootTest
@DisplayName("UserService 통합 테스트 (실제 DB 사용)")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        // 각 테스트 후 DB 데이터 정리 (테스트 격리)
        userRepository.deleteAll();
    }

    /**
     * 사용자 생성 테스트 - 성공
     */
    @Test
    @DisplayName("사용자 생성 - 성공")
    void createUser_Success() {
        // Given
        String name = "홍길동";
        String email = "hong@example.com";
        int age = 25;

        // When
        User createdUser = userService.createUser(name, email, age);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();  // DB에서 자동 생성된 ID
        assertThat(createdUser.getName()).isEqualTo(name);
        assertThat(createdUser.getEmail()).isEqualTo(email);
        assertThat(createdUser.getAge()).isEqualTo(age);
        assertThat(createdUser.getCreatedAt()).isNotNull();
        assertThat(createdUser.getUpdatedAt()).isNotNull();

        // DB에 실제로 저장되었는지 확인
        User foundUser = userRepository.findById(createdUser.getId()).orElseThrow();
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }

    /**
     * 사용자 생성 테스트 - 중복 이메일로 실패
     */
    @Test
    @DisplayName("사용자 생성 - 중복 이메일로 실패")
    void createUser_DuplicateEmail() {
        // Given: 먼저 사용자 생성
        String email = "hong@example.com";
        userService.createUser("홍길동", email, 25);

        // When & Then: 같은 이메일로 다시 생성 시도
        assertThatThrownBy(() -> userService.createUser("김철수", email, 30))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(email);

        // DB에는 한 명만 있어야 함
        assertThat(userRepository.count()).isEqualTo(1);
    }

    /**
     * ID로 사용자 조회 테스트 - 성공
     */
    @Test
    @DisplayName("ID로 사용자 조회 - 성공")
    void getUserById_Success() {
        // Given: DB에 사용자 저장
        User savedUser = userService.createUser("홍길동", "hong@example.com", 25);

        // When
        User foundUser = userService.getUserById(savedUser.getId());

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("hong@example.com");
    }

    /**
     * ID로 사용자 조회 테스트 - 존재하지 않음
     */
    @Test
    @DisplayName("ID로 사용자 조회 - 존재하지 않음")
    void getUserById_NotFound() {
        // Given: 존재하지 않는 ID
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    /**
     * 사용자 수정 테스트 - 성공
     */
    @Test
    @DisplayName("사용자 수정 - 성공")
    @Transactional  // 트랜잭션 테스트를 위해 필요
    void updateUser_Success() {
        // Given: DB에 사용자 저장
        User savedUser = userService.createUser("홍길동", "hong@example.com", 25);

        // When: 정보 수정
        String newName = "홍길동2";
        String newEmail = "hong2@example.com";
        int newAge = 30;
        User updatedUser = userService.updateUser(savedUser.getId(), newName, newEmail, newAge);

        // Then
        assertThat(updatedUser.getName()).isEqualTo(newName);
        assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
        assertThat(updatedUser.getAge()).isEqualTo(newAge);

        // DB에서 다시 조회하여 확인
        User foundUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(foundUser.getName()).isEqualTo(newName);
        assertThat(foundUser.getEmail()).isEqualTo(newEmail);
    }

    /**
     * 사용자 수정 테스트 - 다른 사용자의 이메일로 변경 시도
     */
    @Test
    @DisplayName("사용자 수정 - 중복 이메일로 실패")
    void updateUser_DuplicateEmail() {
        // Given: 두 명의 사용자 저장
        User user1 = userService.createUser("홍길동", "hong@example.com", 25);
        User user2 = userService.createUser("김철수", "kim@example.com", 30);

        // When & Then: user1의 이메일을 user2의 이메일로 변경 시도
        assertThatThrownBy(() ->
                userService.updateUser(user1.getId(), "홍길동", "kim@example.com", 25)
        )
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("kim@example.com");

        // user1의 이메일은 변경되지 않아야 함
        User unchangedUser = userRepository.findById(user1.getId()).orElseThrow();
        assertThat(unchangedUser.getEmail()).isEqualTo("hong@example.com");
    }

    /**
     * 사용자 삭제 테스트 - 성공
     */
    @Test
    @DisplayName("사용자 삭제 - 성공")
    void deleteUser_Success() {
        // Given: DB에 사용자 저장
        User savedUser = userService.createUser("홍길동", "hong@example.com", 25);
        Long userId = savedUser.getId();

        // When: 삭제
        userService.deleteUser(userId);

        // Then: DB에서 삭제되었는지 확인
        assertThat(userRepository.existsById(userId)).isFalse();
        assertThat(userRepository.count()).isEqualTo(0);
    }

    /**
     * 사용자 삭제 테스트 - 존재하지 않는 사용자
     */
    @Test
    @DisplayName("사용자 삭제 - 존재하지 않음")
    void deleteUser_NotFound() {
        // Given: 존재하지 않는 ID
        Long nonExistentId = 999L;

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    /**
     * 이름 검색 테스트
     */
    @Test
    @DisplayName("이름으로 사용자 검색 - 성공")
    void searchUsersByName_Success() {
        // Given: 여러 사용자 저장
        userService.createUser("홍길동", "hong1@example.com", 25);
        userService.createUser("홍길순", "hong2@example.com", 30);
        userService.createUser("김철수", "kim@example.com", 35);

        // When: "홍"으로 검색
        var users = userService.searchUsersByName("홍");

        // Then: 2명 찾아야 함
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("홍길동", "홍길순");
    }

    /**
     * 트랜잭션 롤백 테스트
     * 중간에 예외 발생 시 전체 롤백되는지 확인
     */
    @Test
    @DisplayName("트랜잭션 롤백 테스트 - 중복 이메일로 전체 롤백")
    void transaction_RollbackOnDuplicateEmail() {
        // Given: 먼저 한 명 저장
        userService.createUser("기존유저", "existing@example.com", 25);
        long initialCount = userRepository.count();

        // When & Then: 중복 이메일로 생성 시도 (실패해야 함)
        assertThatThrownBy(() ->
                userService.createUser("신규유저", "existing@example.com", 30)
        ).isInstanceOf(DuplicateEmailException.class);

        // DB 상태가 변하지 않았는지 확인 (롤백됨)
        assertThat(userRepository.count()).isEqualTo(initialCount);
    }
}
