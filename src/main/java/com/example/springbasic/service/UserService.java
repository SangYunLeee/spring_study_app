package com.example.springbasic.service;

import com.example.springbasic.exception.DuplicateEmailException;
import com.example.springbasic.exception.UserNotFoundException;
import com.example.springbasic.model.User;
import com.example.springbasic.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     *
     * @Transactional:
     * - 중복 이메일 체크(조회) + 저장이 하나의 트랜잭션
     * - 중간에 예외 발생 시 자동 롤백
     * - 동시성 문제 방지 (같은 이메일로 동시 가입 시도 시)
     */
    @Transactional
    public User createUser(String name, String email, int age) {
        // 비즈니스 로직 1: 중복 이메일 체크
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException(email);  // Custom Exception 사용
        }

        // 비즈니스 로직 2: User 객체 생성 및 검증 (User의 생성자에서 자동 검증)
        User newUser = User.createNew(User.CreateRequest.of(name, email, age));

        // Repository를 통해 저장
        return userRepository.save(newUser);
    }

    /**
     * ID로 사용자 조회
     *
     * @Transactional(readOnly = true):
     * - 읽기 전용 트랜잭션 (성능 최적화)
     * - DB에 변경 없음을 명시 → flush 생략, 최적화 가능
     *
     * @throws UserNotFoundException 사용자가 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * 모든 사용자 조회 (페이징 지원)
     *
     * Spring Data JPA의 Pageable을 사용한 페이지네이션:
     * - Pageable: 페이지 번호, 크기, 정렬 정보를 담은 인터페이스
     * - Page<T>: 페이지 데이터 + 메타데이터 (총 개수, 총 페이지 등)
     *
     * 사용 예:
     * - PageRequest.of(0, 10) : 첫 페이지, 10개씩
     * - PageRequest.of(0, 10, Sort.by("name").ascending()) : 이름 오름차순
     *
     * @Transactional(readOnly = true): 읽기 전용
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 모든 사용자 조회 (페이징 없음 - 호환성 유지)
     *
     * @Transactional(readOnly = true): 읽기 전용
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 이메일로 사용자 찾기
     *
     * @Transactional(readOnly = true): 읽기 전용
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 이름으로 사용자 검색
     *
     * @Transactional(readOnly = true): 읽기 전용
     */
    @Transactional(readOnly = true)
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
     *
     * @Transactional:
     * - 사용자 조회 + 중복 체크 + 수정이 하나의 트랜잭션
     * - 중간에 실패 시 모든 변경사항 롤백
     * - Dirty Checking: save() 없이도 변경 감지하여 자동 UPDATE
     */
    @Transactional
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

        // 3. 필드 업데이트 (전체 필드 업데이트)
        existingUser.update(User.UpdateRequest.of(name, email, age, null, null));

        // 4. 저장 (JPA가 변경 감지하여 자동 UPDATE)
        return userRepository.save(existingUser);
    }

    /**
     * 사용자 부분 수정 (PATCH)
     * 제공된 필드만 수정
     *
     * @Transactional: updateUser와 동일한 이유
     */
    @Transactional
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

        // 3. 변경된 필드만 업데이트 (null이 아닌 필드만 업데이트)
        existingUser.update(User.UpdateRequest.of(name, email, age, null, null));

        // 4. 저장 (변경된 필드가 있을 경우에만 UPDATE 쿼리 발생)
        return userRepository.save(existingUser);
    }

    /**
     * 사용자 삭제
     *
     * @Transactional:
     * - 존재 확인 + 삭제가 하나의 트랜잭션
     * - 확인과 삭제 사이에 다른 트랜잭션이 끼어들 수 없음
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);  // Custom Exception 사용
        }
        userRepository.deleteById(id);
    }

    /**
     * 전체 사용자 수 조회
     *
     * @Transactional(readOnly = true): 읽기 전용
     */
    @Transactional(readOnly = true)
    public long getUserCount() {
        return userRepository.count();
    }

    /**
     * 성인 사용자만 조회 (비즈니스 로직 예제)
     *
     * @Transactional(readOnly = true): 읽기 전용
     */
    @Transactional(readOnly = true)
    public List<User> getAdultUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getAge() >= 19)
                .toList();
    }

    /**
     * 나이대별 사용자 수 통계 (비즈니스 로직 예제)
     *
     * @Transactional(readOnly = true): 읽기 전용
     */
    @Transactional(readOnly = true)
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