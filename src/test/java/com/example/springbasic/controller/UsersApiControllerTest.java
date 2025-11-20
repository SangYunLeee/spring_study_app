package com.example.springbasic.controller;

import com.example.springbasic.exception.DuplicateEmailException;
import com.example.springbasic.exception.GlobalExceptionHandler;
import com.example.springbasic.exception.UserNotFoundException;
import com.example.springbasic.model.User;
import com.example.springbasic.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UsersApiController 단위 테스트
 *
 * @WebMvcTest:
 * - Controller 계층만 테스트 (Service, Repository 제외)
 * - MockMvc 자동 설정
 * - Spring MVC 컴포넌트만 로드 (빠름!)
 * - @MockBean으로 Service를 Mock으로 대체
 *
 * @Import(GlobalExceptionHandler.class):
 * - 예외 처리기를 테스트 컨텍스트에 포함
 * - Controller에서 발생한 예외를 처리
 *
 * MockMvc:
 * - HTTP 요청을 시뮬레이션
 * - 실제 서버 띄우지 않고 Controller 테스트
 * - perform() → andExpect() 패턴
 *
 * vs @SpringBootTest + @AutoConfigureMockMvc:
 * - 전체 Spring 컨텍스트 로드 (느림)
 * - 실제 Bean 사용
 * - 통합 테스트용
 */
@WebMvcTest(UsersApiController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("UsersApiController 단위 테스트")
class UsersApiControllerTest {

    /**
     * MockMvc: Controller HTTP 요청 시뮬레이션
     * @WebMvcTest가 자동 설정
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper: JSON ↔ 객체 변환
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @MockBean: Service를 Mock으로 대체
     * - Spring 컨텍스트에 Mock 객체 등록
     * - Controller가 이 Mock을 주입받음
     */
    @MockBean
    private UserService userService;

    // ========== POST /api/users (사용자 생성) ==========

    /**
     * 사용자 생성 - 성공
     *
     * HTTP 요청:
     * POST /api/users
     * Content-Type: application/json
     * Body: {"name":"홍길동","email":"hong@example.com","age":25}
     *
     * 예상 응답:
     * 201 Created
     * Location: /api/users/1
     * Body: {"id":1,"name":"홍길동",...}
     */
    @Test
    @DisplayName("POST /api/users - 사용자 생성 성공")
    void createUser_Success() throws Exception {
        // Given: Mock Service 동작 정의
        User createdUser = User.createNew(User.CreateRequest.of("홍길동", "hong@example.com", 25));
        createdUser.setId(1L);

        when(userService.createUser("홍길동", "hong@example.com", 25))
                .thenReturn(createdUser);

        // When & Then: HTTP 요청 & 검증
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "홍길동",
                                    "email": "hong@example.com",
                                    "age": 25
                                }
                                """))
                .andDo(print()) // 요청/응답 로그 출력
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(header().string("Location", "/api/users/1")) // Location 헤더
                .andExpect(jsonPath("$.id").value(1)) // 응답 JSON 검증
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@example.com"))
                .andExpect(jsonPath("$.age").value(25));

        // Verify: Service 메서드 호출 확인
        verify(userService).createUser("홍길동", "hong@example.com", 25);
    }

    /**
     * 사용자 생성 - 중복 이메일로 실패
     *
     * 예상 응답:
     * 400 Bad Request
     * Body: {"status":400,"error":"Bad Request","message":"..."}
     */
    @Test
    @DisplayName("POST /api/users - 중복 이메일로 실패 (400)")
    void createUser_DuplicateEmail() throws Exception {
        // Given: Service에서 예외 발생
        when(userService.createUser(any(), eq("hong@example.com"), anyInt()))
                .thenThrow(new DuplicateEmailException("hong@example.com"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "홍길동",
                                    "email": "hong@example.com",
                                    "age": 25
                                }
                                """))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("hong@example.com")));

        verify(userService).createUser(any(), eq("hong@example.com"), anyInt());
    }

    /**
     * 사용자 생성 - 유효성 검증 실패 (@Valid)
     *
     * Bean Validation 실패 시:
     * - @NotBlank, @Email, @Min, @Max 위반
     * - MethodArgumentNotValidException 발생
     * - GlobalExceptionHandler가 400 반환
     */
    @Test
    @DisplayName("POST /api/users - 유효성 검증 실패 (400)")
    void createUser_ValidationFailed() throws Exception {
        // When & Then: 잘못된 데이터 전송
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "email": "invalid-email",
                                    "age": 200
                                }
                                """))
                .andDo(print())
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.status").value(400));

        // Service 호출되지 않아야 함 (검증 단계에서 실패)
        verify(userService, never()).createUser(any(), any(), anyInt());
    }

    // ========== GET /api/users (전체 조회 - 페이징) ==========

    /**
     * 사용자 목록 조회 - 페이징 성공
     *
     * HTTP 요청:
     * GET /api/users?page=0&size=10&sort=name,asc
     *
     * 예상 응답:
     * 200 OK
     * Body: {"content":[...],"totalElements":2,"totalPages":1,...}
     */
    @Test
    @DisplayName("GET /api/users - 페이징 조회 성공")
    void getAllUsers_Paging_Success() throws Exception {
        // Given: Mock 페이지 데이터
        User user1 = User.createNew(User.CreateRequest.of("홍길동", "hong@example.com", 25));
        user1.setId(1L);
        User user2 = User.createNew(User.CreateRequest.of("김철수", "kim@example.com", 30));
        user2.setId(2L);

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 2);

        when(userService.getAllUsers(any(PageRequest.class)))
                .thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("홍길동"))
                .andExpect(jsonPath("$.content[1].name").value("김철수"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(userService).getAllUsers(any(PageRequest.class));
    }

    // ========== GET /api/users/{id} (ID로 조회) ==========

    /**
     * ID로 사용자 조회 - 성공
     */
    @Test
    @DisplayName("GET /api/users/1 - ID로 조회 성공")
    void getUserById_Success() throws Exception {
        // Given
        User user = User.createNew(User.CreateRequest.of("홍길동", "hong@example.com", 25));
        user.setId(1L);

        when(userService.getUserById(1L))
                .thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.email").value("hong@example.com"));

        verify(userService).getUserById(1L);
    }

    /**
     * ID로 사용자 조회 - 존재하지 않음 (404)
     */
    @Test
    @DisplayName("GET /api/users/999 - 존재하지 않음 (404)")
    void getUserById_NotFound() throws Exception {
        // Given: Service에서 예외 발생
        when(userService.getUserById(999L))
                .thenThrow(new UserNotFoundException(999L));

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andDo(print())
                .andExpect(status().isNotFound()) // 404
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("999")));

        verify(userService).getUserById(999L);
    }

    // ========== PUT /api/users/{id} (전체 수정) ==========

    /**
     * 사용자 전체 수정 - 성공
     */
    @Test
    @DisplayName("PUT /api/users/1 - 전체 수정 성공")
    void updateUser_Success() throws Exception {
        // Given
        User updatedUser = User.createNew(User.CreateRequest.of("홍길동2", "hong2@example.com", 30));
        updatedUser.setId(1L);

        when(userService.updateUser(1L, "홍길동2", "hong2@example.com", 30))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "홍길동2",
                                    "email": "hong2@example.com",
                                    "age": 30
                                }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("홍길동2"))
                .andExpect(jsonPath("$.email").value("hong2@example.com"))
                .andExpect(jsonPath("$.age").value(30));

        verify(userService).updateUser(1L, "홍길동2", "hong2@example.com", 30);
    }

    // ========== PATCH /api/users/{id} (부분 수정) ==========

    /**
     * 사용자 부분 수정 - 성공 (나이만 변경)
     */
    @Test
    @DisplayName("PATCH /api/users/1 - 부분 수정 성공 (나이만)")
    void patchUser_Success() throws Exception {
        // Given: 나이만 변경
        User patchedUser = User.createNew(User.CreateRequest.of("홍길동", "hong@example.com", 26));
        patchedUser.setId(1L);

        when(userService.patchUser(eq(1L), isNull(), isNull(), eq(26)))
                .thenReturn(patchedUser);

        // When & Then
        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "age": 26
                                }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.age").value(26));

        verify(userService).patchUser(eq(1L), isNull(), isNull(), eq(26));
    }

    // ========== DELETE /api/users/{id} (삭제) ==========

    /**
     * 사용자 삭제 - 성공
     *
     * 예상 응답:
     * 204 No Content (본문 없음)
     */
    @Test
    @DisplayName("DELETE /api/users/1 - 삭제 성공 (204)")
    void deleteUser_Success() throws Exception {
        // Given: Service는 void 반환 (doNothing)
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andDo(print())
                .andExpect(status().isNoContent()); // 204

        verify(userService).deleteUser(1L);
    }

    /**
     * 사용자 삭제 - 존재하지 않음 (404)
     */
    @Test
    @DisplayName("DELETE /api/users/999 - 존재하지 않음 (404)")
    void deleteUser_NotFound() throws Exception {
        // Given: Service에서 예외 발생
        doThrow(new UserNotFoundException(999L))
                .when(userService).deleteUser(999L);

        // When & Then
        mockMvc.perform(delete("/api/users/999"))
                .andDo(print())
                .andExpect(status().isNotFound()) // 404
                .andExpect(jsonPath("$.status").value(404));

        verify(userService).deleteUser(999L);
    }

    // ========== GET /api/users/search (검색) ==========

    /**
     * 사용자 이름 검색 - 성공
     */
    @Test
    @DisplayName("GET /api/users/search?keyword=홍 - 검색 성공")
    void searchUsers_Success() throws Exception {
        // Given
        User user1 = User.createNew(User.CreateRequest.of("홍길동", "hong1@example.com", 25));
        user1.setId(1L);
        User user2 = User.createNew(User.CreateRequest.of("홍길순", "hong2@example.com", 30));
        user2.setId(2L);

        when(userService.searchUsersByName("홍"))
                .thenReturn(Arrays.asList(user1, user2));

        // When & Then
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", "홍"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("홍길동"))
                .andExpect(jsonPath("$[1].name").value("홍길순"));

        verify(userService).searchUsersByName("홍");
    }

    // ========== GET /api/users/adults (성인만 조회) ==========

    /**
     * 성인 사용자만 조회 - 성공
     */
    @Test
    @DisplayName("GET /api/users/adults - 성인만 조회")
    void getAdultUsers_Success() throws Exception {
        // Given: 19세 이상만
        User adult = User.createNew(User.CreateRequest.of("홍길동", "hong@example.com", 25));
        adult.setId(1L);

        when(userService.getAdultUsers())
                .thenReturn(List.of(adult));

        // When & Then
        mockMvc.perform(get("/api/users/adults"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("홍길동"))
                .andExpect(jsonPath("$[0].age").value(25));

        verify(userService).getAdultUsers();
    }
}
