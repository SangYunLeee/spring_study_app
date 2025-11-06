package com.example.springbasic.repository;

import com.example.springbasic.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 사용자 데이터를 저장하고 조회하는 Repository
 *
 * @Repository: 데이터 접근 계층을 나타내는 어노테이션
 * - Spring이 자동으로 Bean으로 등록
 * - 데이터베이스나 외부 저장소와의 통신을 담당
 * - 현재는 메모리(Map)에 저장하지만, 나중에 JPA로 쉽게 변경 가능
 */
@Repository
public class UserRepository {

    // 메모리에 사용자 데이터를 저장하는 Map (동시성 안전)
    private final Map<Long, User> storage = new ConcurrentHashMap<>();

    // ID 자동 증가를 위한 카운터 (동시성 안전)
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 사용자 저장
     * ID가 없으면 자동으로 생성하여 할당
     */
    public User save(User user) {
        if (user.id() == null) {
            // 새로운 사용자 - ID 생성
            Long newId = idGenerator.getAndIncrement();
            User newUser = user.withId(newId);
            storage.put(newId, newUser);
            return newUser;
        } else {
            // 기존 사용자 업데이트
            storage.put(user.id(), user);
            return user;
        }
    }

    /**
     * ID로 사용자 찾기
     * Optional을 반환하여 null 처리를 명확하게 함
     */
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * 모든 사용자 조회
     */
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * 이메일로 사용자 찾기
     */
    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    /**
     * 이름으로 사용자 검색 (부분 일치)
     */
    public List<User> findByNameContaining(String keyword) {
        return storage.values().stream()
                .filter(user -> user.name().contains(keyword))
                .toList();
    }

    /**
     * 사용자 삭제
     * @return 삭제 여부
     */
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    /**
     * 모든 사용자 삭제 (테스트용)
     */
    public void deleteAll() {
        storage.clear();
    }

    /**
     * 전체 사용자 수
     */
    public long count() {
        return storage.size();
    }

    /**
     * 사용자 존재 여부 확인
     */
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }
}