package com.example.springbasic.controller;

import com.example.springbasic.api.UsersApi;
import com.example.springbasic.api.model.*;
import com.example.springbasic.model.User;
import com.example.springbasic.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    /**
     * POST /api/users - 사용자 생성
     * 요청 데이터로 새 사용자를 DB에 저장하고 생성된 정보를 반환
     *
     * 예외 처리:
     * - DuplicateEmailException → GlobalExceptionHandler가 400 반환
     * - try-catch 불필요!
     */
    @Override
    public ResponseEntity<UserResponse> createUser(
            com.example.springbasic.api.model.CreateUserRequest createUserRequest
    ) {
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
    }

    /**
     * GET /api/users - 전체 사용자 목록 조회 (페이징 & 정렬 지원)
     *
     * Spring Data JPA의 Pageable을 사용한 페이지네이션:
     * - page: 페이지 번호 (0부터 시작)
     * - size: 한 페이지당 항목 수
     * - sort: 정렬 기준 (예: "name,asc" 또는 "age,desc")
     *
     * 사용 예:
     * - GET /api/users?page=0&size=10
     * - GET /api/users?page=1&size=20&sort=name,asc
     * - GET /api/users?sort=age,desc
     */
    @Override
    public ResponseEntity<PagedUserResponse> getAllUsers(Integer page, Integer size, String sort) {
        // 1. Pageable 객체 생성 (페이지, 크기, 정렬)
        Pageable pageable = createPageable(page, size, sort);

        // 2. Service에서 페이징된 데이터 조회
        Page<User> userPage = userService.getAllUsers(pageable);

        // 3. Page<User> → PagedUserResponse 변환
        PagedUserResponse response = new PagedUserResponse()
                .content(userPage.getContent().stream()
                        .map(this::mapToUserResponse)
                        .toList())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .number(userPage.getNumber())
                .size(userPage.getSize())
                .first(userPage.isFirst())
                .last(userPage.isLast());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users/{id} - 특정 사용자 조회
     * ID로 사용자를 찾아서 반환 (없으면 404)
     *
     * 예외 처리:
     * - UserNotFoundException → GlobalExceptionHandler가 404 + 에러 메시지 반환
     */
    @Override
    public ResponseEntity<UserResponse> getUserById(Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapToUserResponse(user));
    }

    /**
     * GET /api/users/search?keyword={keyword} - 사용자 이름 검색
     * 이름에 키워드가 포함된 사용자들을 찾아서 반환
     */
    @Override
    public ResponseEntity<List<UserResponse>> searchUsers(String keyword) {
        List<UserResponse> users = userService.searchUsersByName(keyword).stream()
                .map(this::mapToUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/adults - 성인 사용자만 조회
     * 나이 19세 이상인 사용자들만 필터링해서 반환
     */
    @Override
    public ResponseEntity<List<UserResponse>> getAdultUsers() {
        List<UserResponse> users = userService.getAdultUsers().stream()
                .map(this::mapToUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/statistics - 사용자 통계 조회
     * 전체 인원, 성인 수, 평균/최소/최대 나이 등을 집계해서 반환
     */
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

    /**
     * PUT /api/users/{id} - 사용자 전체 수정
     * 모든 필드를 새 값으로 교체 (부분 수정은 PATCH 사용)
     *
     * 예외 처리:
     * - UserNotFoundException → GlobalExceptionHandler가 404 반환
     * - DuplicateEmailException → GlobalExceptionHandler가 400 반환
     */
    @Override
    public ResponseEntity<UserResponse> updateUser(
            Long id,
            com.example.springbasic.api.model.UpdateUserRequest updateUserRequest
    ) {
        User updatedUser = userService.updateUser(
                id,
                updateUserRequest.getName(),
                updateUserRequest.getEmail(),
                updateUserRequest.getAge()
        );

        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    /**
     * PATCH /api/users/{id} - 사용자 부분 수정
     * 제공된 필드만 수정하고 나머지는 유지 (최소 1개 필드 필요)
     *
     * 예외 처리:
     * - UserNotFoundException → GlobalExceptionHandler가 404 반환
     * - DuplicateEmailException → GlobalExceptionHandler가 400 반환
     */
    @Override
    public ResponseEntity<UserResponse> patchUser(
            Long id,
            com.example.springbasic.api.model.PatchUserRequest patchUserRequest
    ) {
        // 적어도 하나의 필드는 있어야 함
        if (!hasAtLeastOneField(patchUserRequest)) {
            return ResponseEntity.badRequest().build();
        }

        User patchedUser = userService.patchUser(
                id,
                patchUserRequest.getName(),
                patchUserRequest.getEmail(),
                patchUserRequest.getAge()
        );

        return ResponseEntity.ok(mapToUserResponse(patchedUser));
    }

    /**
     * DELETE /api/users/{id} - 사용자 삭제
     * 해당 ID의 사용자를 DB에서 영구 삭제
     *
     * 예외 처리:
     * - UserNotFoundException → GlobalExceptionHandler가 404 반환
     */
    @Override
    public ResponseEntity<Void> deleteUser(Long id) {
        userService.deleteUser(id);
        // 204 No Content
        return ResponseEntity.noContent().build();
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

    /**
     * Pageable 객체 생성
     *
     * sort 파라미터 파싱:
     * - "name,asc" → Sort.by("name").ascending()
     * - "age,desc" → Sort.by("age").descending()
     * - null → 정렬 없음 (기본 순서)
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param sort 정렬 기준 (형식: "필드명,방향")
     * @return Pageable 객체
     */
    private Pageable createPageable(Integer page, Integer size, String sort) {
        // 정렬 파싱
        if (sort != null && !sort.isBlank()) {
            String[] sortParams = sort.split(",");
            String property = sortParams[0];  // 필드명
            String direction = sortParams.length > 1 ? sortParams[1] : "asc";  // 방향 (기본값: asc)

            Sort sortObj = direction.equalsIgnoreCase("desc")
                    ? Sort.by(property).descending()
                    : Sort.by(property).ascending();

            return PageRequest.of(page, size, sortObj);
        }

        // 정렬 없음
        return PageRequest.of(page, size);
    }
}