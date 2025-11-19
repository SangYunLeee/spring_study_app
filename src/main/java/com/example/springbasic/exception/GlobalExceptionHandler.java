package com.example.springbasic.exception;

import com.example.springbasic.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리기 (Global Exception Handler)
 *
 * @RestControllerAdvice의 역할:
 * - 모든 @RestController에서 발생하는 예외를 한 곳에서 처리
 * - 각 컨트롤러에서 try-catch 반복 불필요
 * - 예외 타입별로 다른 HTTP 상태 코드 반환
 *
 * 동작 원리:
 * 1. Controller에서 예외 발생
 * 2. Spring이 예외를 캐치
 * 3. @ExceptionHandler 중 매칭되는 메서드 찾기
 * 4. 해당 메서드 실행 → ResponseEntity 반환
 * 5. 클라이언트에게 에러 응답 전달
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 404 Not Found - 리소스를 찾을 수 없음
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * 400 Bad Request - @Valid 유효성 검증 실패
     *
     * 발생 시점: @Valid 어노테이션으로 DTO 검증 실패
     * 예: 이메일 형식 오류, 나이 범위 초과 등
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        // 모든 검증 오류 메시지를 하나로 합치기
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    /**
     * 400 Bad Request - 중복된 이메일
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * 400 Bad Request - 유효하지 않은 데이터
     */
    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserData(
            InvalidUserDataException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * 400 Bad Request - 일반적인 잘못된 요청
     * (기존 코드와의 호환성 유지)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * 500 Internal Server Error - 예상하지 못한 에러
     * (마지막 안전장치)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        // 실무에서는 여기에 로깅 추가
        // log.error("Unexpected error", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다: " + ex.getMessage(),
                request
        );
    }

    /**
     * 공통 ErrorResponse 생성 헬퍼 메서드
     * - 중복 코드 제거
     * - 일관된 응답 형식 보장
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }
}
