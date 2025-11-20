package com.example.springbasic.service;

import com.example.springbasic.exception.DuplicateEmailException;
import com.example.springbasic.exception.UserNotFoundException;
import com.example.springbasic.model.User;
import com.example.springbasic.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트
 *
 * 단위 테스트란?
 * - 하나의 클래스(또는 메서드)만 독립적으로 테스트
 * - 의존성은 Mock 객체로 대체
 * - 빠르게 실행 (DB, Spring 컨텍스트 불필요)
 * - 테스트 격리 (다른 컴포넌트 영향 없음)
 *
 * vs 통합 테스트 (UserServiceIntegrationTest)
 * - 실제 DB, Spring 컨텍스트 사용
 * - 느림 (Spring 로딩 + DB 연결)
 * - 전체 흐름 검증
 *
 * @ExtendWith(MockitoExtension.class)
 * - JUnit 5에서 Mockito를 사용하기 위한 확장
 * - @Mock, @InjectMocks 어노테이션 활성화
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    /**
     * @Mock: Mock 객체 생성
     * - 실제 UserRepository 대신 가짜 객체 사용
     * - 동작을 when().thenReturn()으로 정의 가능
     */
    @Mock
    private UserRepository userRepository;

    /**
     * @InjectMocks: Mock 객체를 주입받는 테스트 대상
     * - UserService에 @Mock userRepository를 자동 주입
     * - 실제 UserService 객체 생성 (Mock 의존성과 함께)
     */
    @InjectMocks
    private UserService userService;

    // ========== createUser 테스트 ==========

    /**
     * 사용자 생성 - 성공 케이스
     *
     * Given: 이메일 중복 없음
     * When: createUser 호출
     * Then: 사용자 생성 성공
     */
    @Test
    @DisplayName("사용자 생성 - 성공")
    void createUser_Success() {
        // Given: 테스트 데이터 준비
        String name = "홍길동";
        String email = "hong@example.com";
        int age = 25;

        // Mock 동작 정의 1: 중복 이메일 체크 → 없음
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Mock 동작 정의 2: 저장 → 성공 (ID가 할당된 User 반환)
        User savedUser = User.createNew(User.CreateRequest.of(name, email, age));
        savedUser.setId(1L); // ID 설정 (실제로는 DB가 자동 생성)
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // When: 실제 테스트 대상 메서드 호출
        User result = userService.createUser(name, email, age);

        // Then: 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getAge()).isEqualTo(age);

        // Mock 호출 검증
        verify(userRepository).findByEmail(email); // findByEmail이 1번 호출되었는지
        verify(userRepository).save(any(User.class)); // save가 1번 호출되었는지
    }

    /**
     * 사용자 생성 - 중복 이메일로 실패
     *
     * Given: 이미 존재하는 이메일
     * When: createUser 호출
     * Then: DuplicateEmailException 발생
     */
    @Test
    @DisplayName("사용자 생성 - 중복 이메일로 실패")
    void createUser_DuplicateEmail() {
        // Given
        String email = "hong@example.com";
        User existingUser = User.createNew(User.CreateRequest.of("기존유저", email, 30));

        // Mock 동작: 이미 존재하는 사용자 반환
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(existingUser));

        // When & Then: 예외 발생 검증
        assertThatThrownBy(() -> userService.createUser("신규유저", email, 25))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining(email);

        // save는 호출되지 않아야 함 (중복 체크에서 실패)
        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== getUserById 테스트 ==========

    /**
     * ID로 사용자 조회 - 성공
     *
     * Given: 존재하는 ID
     * When: getUserById 호출
     * Then: 사용자 반환
     */
    @Test
    @DisplayName("ID로 사용자 조회 - 성공")
    void getUserById_Success() {
        // Given
        Long userId = 1L;
        User mockUser = User.createNew(User.CreateRequest.of("홍길동", "hong@example.com", 25));
        mockUser.setId(userId);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));

        // When
        User result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("홍길동");

        verify(userRepository).findById(userId);
    }

    /**
     * ID로 사용자 조회 - 존재하지 않음
     *
     * Given: 존재하지 않는 ID
     * When: getUserById 호출
     * Then: UserNotFoundException 발생
     */
    @Test
    @DisplayName("ID로 사용자 조회 - 존재하지 않음")
    void getUserById_NotFound() {
        // Given
        Long nonExistentId = 999L;

        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository).findById(nonExistentId);
    }

    // ========== updateUser 테스트 ==========

    /**
     * 사용자 수정 - 성공
     *
     * Given: 존재하는 사용자, 중복 없는 새 이메일
     * When: updateUser 호출
     * Then: 수정 성공
     */
    @Test
    @DisplayName("사용자 수정 - 성공")
    void updateUser_Success() {
        // Given
        Long userId = 1L;
        User existingUser = User.createNew(User.CreateRequest.of("홍길동", "hong@example.com", 25));
        existingUser.setId(userId);

        String newName = "홍길동2";
        String newEmail = "hong2@example.com";
        int newAge = 30;

        // Mock 동작 1: 기존 사용자 찾기
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // Mock 동작 2: 새 이메일 중복 체크 → 없음
        when(userRepository.findByEmail(newEmail))
                .thenReturn(Optional.empty());

        // Mock 동작 3: 저장
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // 입력받은 객체 그대로 반환

        // When
        User result = userService.updateUser(userId, newName, newEmail, newAge);

        // Then
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getAge()).isEqualTo(newAge);

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(existingUser);
    }

    /**
     * 사용자 수정 - 존재하지 않는 사용자
     */
    @Test
    @DisplayName("사용자 수정 - 존재하지 않음")
    void updateUser_UserNotFound() {
        // Given
        Long nonExistentId = 999L;

        when(userRepository.findById(nonExistentId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                userService.updateUser(nonExistentId, "이름", "email@test.com", 25)
        )
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * 사용자 수정 - 다른 사용자의 이메일로 변경 시도
     */
    @Test
    @DisplayName("사용자 수정 - 중복 이메일로 실패")
    void updateUser_DuplicateEmail() {
        // Given
        Long userId = 1L;
        User existingUser = User.createNew(User.CreateRequest.of("홍길동", "hong@example.com", 25));
        existingUser.setId(userId);

        User anotherUser = User.createNew(User.CreateRequest.of("김철수", "kim@example.com", 30));
        anotherUser.setId(2L);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(existingUser));

        // 다른 사용자가 이미 사용 중인 이메일
        when(userRepository.findByEmail("kim@example.com"))
                .thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() ->
                userService.updateUser(userId, "홍길동", "kim@example.com", 25)
        )
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail("kim@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== deleteUser 테스트 ==========

    /**
     * 사용자 삭제 - 성공
     */
    @Test
    @DisplayName("사용자 삭제 - 성공")
    void deleteUser_Success() {
        // Given
        Long userId = 1L;

        when(userRepository.existsById(userId))
                .thenReturn(true);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    /**
     * 사용자 삭제 - 존재하지 않는 사용자
     */
    @Test
    @DisplayName("사용자 삭제 - 존재하지 않음")
    void deleteUser_NotFound() {
        // Given
        Long nonExistentId = 999L;

        when(userRepository.existsById(nonExistentId))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(nonExistentId);
        verify(userRepository, never()).deleteById(any());
    }
}
