package com.example.springbasic.controller;

import com.example.springbasic.api.UsersApi;
import com.example.springbasic.api.model.*;
import com.example.springbasic.model.User;
import com.example.springbasic.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * 명세 우선 개발 - 생성된 API 인터페이스 구현
 *
 * 이 컨트롤러는 OpenAPI 명세에서 자동 생성된 UsersApi 인터페이스를 구현합니다.
 * 명세가 변경되면 인터페이스가 자동으로 갱신되므로, 구현도 그에 맞춰 수정됩니다.
 *
 * 워크플로우:
 * 1. api-spec.yaml 명세 작성/수정
 * 2. ./gradlew generateApi 실행
 * 3. 생성된 UsersApi 인터페이스 확인
 * 4. 이 컨트롤러에서 구현 (현재 파일)
 */
@RestController
public class UsersApiController implements UsersApi {

    private final UserService userService;

    public UsersApiController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserResponse> createUser(
            com.example.springbasic.api.model.CreateUserRequest createUserRequest
    ) {
        try {
            // 생성된 API 모델 → 도메인 모델 변환
            User user = userService.createUser(
                    createUserRequest.getName(),
                    createUserRequest.getEmail(),
                    createUserRequest.getAge()
            );

            // 도메인 모델 → API 응답 모델 변환
            UserResponse response = mapToUserResponse(user);

            // 201 Created + Location 헤더
            return ResponseEntity
                    .created(URI.create("/api/users/" + user.getId()))
                    .body(response);

        } catch (IllegalArgumentException e) {
            // 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers().stream()
                .map(this::mapToUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        return userService.getUserById(id)
                .map(this::mapToUserResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<UserResponse>> searchUsers(String keyword) {
        List<UserResponse> users = userService.searchUsersByName(keyword).stream()
                .map(this::mapToUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<List<UserResponse>> getAdultUsers() {
        List<UserResponse> users = userService.getAdultUsers().stream()
                .map(this::mapToUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<com.example.springbasic.api.model.UserStatistics> getUserStatistics() {
        UserService.UserStatistics stats = userService.getUserStatistics();

        // 도메인 통계 → API 통계 모델 변환
        com.example.springbasic.api.model.UserStatistics response =
                new com.example.springbasic.api.model.UserStatistics()
                        .totalCount(stats.totalCount())
                        .adultCount(stats.adultCount())
                        .averageAge(stats.averageAge())
                        .minAge(stats.minAge())
                        .maxAge(stats.maxAge());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(
            Long id,
            com.example.springbasic.api.model.UpdateUserRequest updateUserRequest
    ) {
        try {
            User updatedUser = userService.updateUser(
                    id,
                    updateUserRequest.getName(),
                    updateUserRequest.getEmail(),
                    updateUserRequest.getAge()
            );

            return ResponseEntity.ok(mapToUserResponse(updatedUser));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<UserResponse> patchUser(
            Long id,
            com.example.springbasic.api.model.PatchUserRequest patchUserRequest
    ) {
        // 적어도 하나의 필드는 있어야 함
        if (!hasAtLeastOneField(patchUserRequest)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            User patchedUser = userService.patchUser(
                    id,
                    patchUserRequest.getName(),
                    patchUserRequest.getEmail(),
                    patchUserRequest.getAge()
            );

            return ResponseEntity.ok(mapToUserResponse(patchedUser));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        try {
            userService.deleteUser(id);
            // 204 No Content
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            // 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    // ========== 헬퍼 메서드 ==========

    /**
     * 도메인 User → API UserResponse 변환
     */
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge());
    }

    /**
     * PatchUserRequest가 적어도 하나의 필드를 가지고 있는지 확인
     */
    private boolean hasAtLeastOneField(com.example.springbasic.api.model.PatchUserRequest request) {
        return request.getName() != null ||
                request.getEmail() != null ||
                request.getAge() != null;
    }
}