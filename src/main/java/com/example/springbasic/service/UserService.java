package com.example.springbasic.service;

import com.example.springbasic.exception.DuplicateEmailException;
import com.example.springbasic.exception.UserNotFoundException;
import com.example.springbasic.model.User;
import com.example.springbasic.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 관련 비즈니스 로직을 담당하는 Service
 *
 * Service의 역할:
 * 1. 비즈니스 로직 처리 (검증, 계산, 변환 등)
 * 2. 트랜잭션 관리 (@Transactional)
 * 3. 여러 Repository를 조합하여 복잡한 작업 수행
 * 4. Controller와 Repository 사이의 중간 계층
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    /**
     * 생성자 주입 (권장 방식)
     * - final로 선언하여 불변성 보장
     * - 테스트 시 Mock 객체 주입이 쉬움
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 사용자 생성
     * 비즈니스 규칙: 중복 이메일 체크
     */
    public User createUser(String name, String email, int age) {
        // 비즈니스 로직 1: 중복 이메일 체크
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException(email);  // Custom Exception 사용
        }

        // 비즈니스 로직 2: User 객체 생성 및 검증 (User의 생성자에서 자동 검증)
        User newUser = User.createNew(name, email, age);

        // Repository를 통해 저장
        return userRepository.save(newUser);
    }

    /**
     * ID로 사용자 조회
     *
     * @throws UserNotFoundException 사용자가 존재하지 않을 경우
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * 모든 사용자 조회
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 이메일로 사용자 찾기
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 이름으로 사용자 검색
     */
    public List<User> searchUsersByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요");
        }
        return userRepository.findByNameContaining(keyword);
    }

    /**
     * 사용자 정보 수정
     * 비즈니스 규칙:
     * 1. 사용자가 존재해야 함
     * 2. 다른 사용자의 이메일로 변경 불가
     */
    public User updateUser(Long id, String name, String email, int age) {
        // 1. 사용자 존재 확인
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));  // Custom Exception 사용

        // 2. 이메일 변경 시 중복 체크
        if (!existingUser.getEmail().equals(email)) {
            userRepository.findByEmail(email).ifPresent(user -> {
                throw new DuplicateEmailException(email);  // Custom Exception 사용
            });
        }

        // 3. 필드 업데이트 (JPA Entity는 setter 사용)
        existingUser.setName(name);
        existingUser.setEmail(email);
        existingUser.setAge(age);

        // 4. 저장 (JPA가 변경 감지하여 자동 UPDATE)
        return userRepository.save(existingUser);
    }

    /**
     * 사용자 부분 수정 (PATCH)
     * 제공된 필드만 수정
     */
    public User patchUser(Long id, String name, String email, Integer age) {
        // 1. 사용자 존재 확인
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));  // Custom Exception 사용

        // 2. 이메일 변경 시 중복 체크
        if (email != null && !existingUser.getEmail().equals(email)) {
            userRepository.findByEmail(email).ifPresent(user -> {
                throw new DuplicateEmailException(email);  // Custom Exception 사용
            });
        }

        // 3. 변경된 필드만 업데이트 (null이 아닌 것만)
        if (name != null) {
            existingUser.setName(name);
        }
        if (email != null) {
            existingUser.setEmail(email);
        }
        if (age != null) {
            existingUser.setAge(age);
        }

        // 4. 저장 (JPA가 변경 감지하여 자동 UPDATE)
        return userRepository.save(existingUser);
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);  // Custom Exception 사용
        }
        userRepository.deleteById(id);
    }

    /**
     * 전체 사용자 수 조회
     */
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * 성인 사용자만 조회 (비즈니스 로직 예제)
     */
    public List<User> getAdultUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getAge() >= 19)
                .toList();
    }

    /**
     * 나이대별 사용자 수 통계 (비즈니스 로직 예제)
     */
    public UserStatistics getUserStatistics() {
        List<User> allUsers = userRepository.findAll();

        if (allUsers.isEmpty()) {
            return new UserStatistics(0, 0, 0.0, 0, 0);
        }

        long totalCount = allUsers.size();
        long adultCount = allUsers.stream().filter(u -> u.getAge() >= 19).count();
        double averageAge = allUsers.stream()
                .mapToInt(User::getAge)
                .average()
                .orElse(0.0);
        int minAge = allUsers.stream().mapToInt(User::getAge).min().orElse(0);
        int maxAge = allUsers.stream().mapToInt(User::getAge).max().orElse(0);

        return new UserStatistics(totalCount, adultCount, averageAge, minAge, maxAge);
    }

    /**
     * 사용자 통계 정보를 담는 record
     */
    public record UserStatistics(
            long totalCount,
            long adultCount,
            double averageAge,
            int minAge,
            int maxAge
    ) {
    }
}