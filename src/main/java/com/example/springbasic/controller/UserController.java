package com.example.springbasic.controller;

import com.example.springbasic.model.User;
import com.example.springbasic.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 관리 API 컨트롤러
 *
 * Controller의 역할:
 * 1. HTTP 요청 받기
 * 2. 요청 데이터 검증 (간단한 것만)
 * 3. Service 호출
 * 4. HTTP 응답 반환
 *
 * 주의: 비즈니스 로직은 Service에!
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * 생성자 주입
     * Controller -> Service -> Repository 순으로 의존성 주입
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 사용자 생성 API (단순 버전 - GET 사용)
     * 예: GET /api/users/create?name=홍길동&email=hong@example.com&age=25
     *
     * 나중에 POST + @RequestBody로 개선할 예정
     */
    @GetMapping("/create")
    public ResponseEntity<User> createUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam int age
    ) {
        try {
            User user = userService.createUser(name, email, age);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            // 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 모든 사용자 조회
     * GET /api/users
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * ID로 사용자 조회
     * GET /api/users/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)  // 200 OK
                .orElse(ResponseEntity.notFound().build());  // 404 Not Found
    }

    /**
     * 이메일로 사용자 조회
     * GET /api/users/email?email=hong@example.com
     */
    @GetMapping("/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 이름으로 사용자 검색
     * GET /api/users/search?keyword=홍
     */
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword) {
        return userService.searchUsersByName(keyword);
    }

    /**
     * 사용자 정보 수정 (단순 버전 - GET 사용)
     * GET /api/users/1/update?name=김철수&email=kim@example.com&age=30
     *
     * 나중에 PUT + @RequestBody로 개선할 예정
     */
    @GetMapping("/{id}/update")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam int age
    ) {
        try {
            User updatedUser = userService.updateUser(id, name, email, age);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 사용자 삭제
     * GET /api/users/1/delete
     *
     * 나중에 DELETE 메서드로 개선할 예정
     */
    @GetMapping("/{id}/delete")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();  // 204 No Content
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();  // 404 Not Found
        }
    }

    /**
     * 전체 사용자 수 조회
     * GET /api/users/count
     */
    @GetMapping("/count")
    public CountResponse getUserCount() {
        long count = userService.getUserCount();
        return new CountResponse(count);
    }

    /**
     * 성인 사용자만 조회
     * GET /api/users/adults
     */
    @GetMapping("/adults")
    public List<User> getAdultUsers() {
        return userService.getAdultUsers();
    }

    /**
     * 사용자 통계 조회
     * GET /api/users/statistics
     */
    @GetMapping("/statistics")
    public UserService.UserStatistics getUserStatistics() {
        return userService.getUserStatistics();
    }

    /**
     * 사용자 수를 담는 응답 객체
     */
    record CountResponse(long count) {
    }
}