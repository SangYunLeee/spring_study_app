package com.example.springbasic.controller;

import com.example.springbasic.dto.CreateUserRequest;
import com.example.springbasic.dto.PatchUserRequest;
import com.example.springbasic.dto.UpdateUserRequest;
import com.example.springbasic.dto.UserResponse;
import com.example.springbasic.model.User;
import com.example.springbasic.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * RESTful 사용자 관리 API 컨트롤러
 *
 * RESTful API 원칙:
 * 1. 적절한 HTTP 메서드 사용 (GET, POST, PUT, PATCH, DELETE)
 * 2. 명사형 URI 사용 (/users, NOT /getUsers)
 * 3. 적절한 HTTP 상태 코드 반환
 * 4. @RequestBody로 JSON 데이터 받기
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ========== POST - 생성 ==========

    /**
     * 사용자 생성 (RESTful)
     *
     * POST /api/users
     * Content-Type: application/json
     *
     * 요청 본문:
     * {
     *   "name": "홍길동",
     *   "email": "hong@example.com",
     *   "age": 25
     * }
     *
     * 응답: 201 Created
     * Location: /api/users/1
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(
                    request.name(),
                    request.email(),
                    request.age()
            );

            UserResponse response = UserResponse.from(user);

            // 201 Created + Location 헤더
            return ResponseEntity
                    .created(URI.create("/api/users/" + user.id()))
                    .body(response);

        } catch (IllegalArgumentException e) {
            // 400 Bad Request (유효성 검증 실패, 중복 이메일 등)
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== GET - 조회 ==========

    /**
     * 전체 사용자 조회
     *
     * GET /api/users
     *
     * 응답: 200 OK
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(UserResponse::from)
                .toList();

        return ResponseEntity.ok(users);
    }

    /**
     * ID로 사용자 조회
     *
     * GET /api/users/{id}
     *
     * 응답:
     * - 200 OK (사용자 존재)
     * - 404 Not Found (사용자 없음)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 이메일로 사용자 조회
     *
     * GET /api/users/search/email?email=hong@example.com
     */
    @GetMapping("/search/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 이름으로 사용자 검색
     *
     * GET /api/users/search?keyword=홍
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String keyword) {
        List<UserResponse> users = userService.searchUsersByName(keyword).stream()
                .map(UserResponse::from)
                .toList();

        return ResponseEntity.ok(users);
    }

    /**
     * 성인 사용자만 조회
     *
     * GET /api/users/adults
     */
    @GetMapping("/adults")
    public ResponseEntity<List<UserResponse>> getAdultUsers() {
        List<UserResponse> users = userService.getAdultUsers().stream()
                .map(UserResponse::from)
                .toList();

        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 통계 조회
     *
     * GET /api/users/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<UserService.UserStatistics> getUserStatistics() {
        return ResponseEntity.ok(userService.getUserStatistics());
    }

    // ========== PUT - 전체 수정 ==========

    /**
     * 사용자 정보 전체 수정
     *
     * PUT /api/users/{id}
     * Content-Type: application/json
     *
     * 요청 본문 (모든 필드 필수):
     * {
     *   "name": "김철수",
     *   "email": "kim@example.com",
     *   "age": 30
     * }
     *
     * 응답:
     * - 200 OK (수정 성공)
     * - 400 Bad Request (유효성 검증 실패)
     * - 404 Not Found (사용자 없음)
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {
        try {
            User updatedUser = userService.updateUser(
                    id,
                    request.name(),
                    request.email(),
                    request.age()
            );

            return ResponseEntity.ok(UserResponse.from(updatedUser));

        } catch (IllegalArgumentException e) {
            // 404 또는 400
            // 실제로는 예외 메시지를 보고 구분해야 함 (다음 레벨에서 배움)
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== PATCH - 부분 수정 ==========

    /**
     * 사용자 정보 부분 수정
     *
     * PATCH /api/users/{id}
     * Content-Type: application/json
     *
     * 요청 본문 (모든 필드 선택적):
     * {
     *   "age": 31
     * }
     * 또는
     * {
     *   "name": "이름만 변경",
     *   "age": 31
     * }
     *
     * 응답:
     * - 200 OK (수정 성공)
     * - 400 Bad Request (유효성 검증 실패 또는 필드가 하나도 없음)
     * - 404 Not Found (사용자 없음)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> patchUser(
            @PathVariable Long id,
            @RequestBody PatchUserRequest request
    ) {
        // 적어도 하나의 필드는 있어야 함
        if (!request.hasAtLeastOneField()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            User patchedUser = userService.patchUser(
                    id,
                    request.name(),
                    request.email(),
                    request.age()
            );

            return ResponseEntity.ok(UserResponse.from(patchedUser));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========== DELETE - 삭제 ==========

    /**
     * 사용자 삭제
     *
     * DELETE /api/users/{id}
     *
     * 응답:
     * - 204 No Content (삭제 성공)
     * - 404 Not Found (사용자 없음)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            // 204 No Content - 성공했지만 반환할 내용 없음
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            // 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
}