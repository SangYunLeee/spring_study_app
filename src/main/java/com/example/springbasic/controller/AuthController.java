package com.example.springbasic.controller;

import com.example.springbasic.api.AuthApi;
import com.example.springbasic.api.model.AuthResponse;
import com.example.springbasic.api.model.LoginRequest;
import com.example.springbasic.api.model.RegisterRequest;
import com.example.springbasic.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 API Controller
 *
 * 역할:
 * - OpenAPI Generator가 생성한 AuthApi 인터페이스 구현
 * - HTTP 요청/응답 처리
 * - API DTO ↔ Domain 변환
 * - AuthService에 비즈니스 로직 위임
 *
 * 계층 분리:
 * - Controller: HTTP 처리만
 * - Service: 비즈니스 로직
 */
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 회원가입 API 구현
     *
     * API DTO → Service DTO 변환:
     * - RegisterRequest (API) → AuthService.RegisterRequest (Domain)
     *
     * Service DTO → API DTO 변환:
     * - AuthService.AuthResponse (Domain) → AuthResponse (API)
     */
    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        // 1. API DTO → Service DTO 변환
        AuthService.RegisterRequest serviceRequest = AuthService.RegisterRequest.of(
                registerRequest.getName(),
                registerRequest.getEmail(),
                registerRequest.getAge(),
                registerRequest.getPassword()
        );

        // 2. Service 호출
        AuthService.AuthResponse serviceResponse = authService.register(serviceRequest);

        // 3. Service DTO → API DTO 변환
        AuthResponse apiResponse = new AuthResponse()
                .token(serviceResponse.token())
                .userId(serviceResponse.userId())
                .email(serviceResponse.email())
                .name(serviceResponse.name())
                .role(AuthResponse.RoleEnum.fromValue(serviceResponse.role()));

        // 4. HTTP 201 Created 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    /**
     * 로그인 API 구현
     *
     * API DTO → Service DTO 변환:
     * - LoginRequest (API) → AuthService.LoginRequest (Domain)
     *
     * Service DTO → API DTO 변환:
     * - AuthService.AuthResponse (Domain) → AuthResponse (API)
     */
    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        // 1. API DTO → Service DTO 변환
        AuthService.LoginRequest serviceRequest = AuthService.LoginRequest.of(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        // 2. Service 호출
        AuthService.AuthResponse serviceResponse = authService.login(serviceRequest);

        // 3. Service DTO → API DTO 변환
        AuthResponse apiResponse = new AuthResponse()
                .token(serviceResponse.token())
                .userId(serviceResponse.userId())
                .email(serviceResponse.email())
                .name(serviceResponse.name())
                .role(AuthResponse.RoleEnum.fromValue(serviceResponse.role()));

        // 4. HTTP 200 OK 반환
        return ResponseEntity.ok(apiResponse);
    }
}