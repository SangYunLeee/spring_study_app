package com.example.springbasic.dto;

import java.time.LocalDateTime;

/**
 * API 에러 응답 표준 포맷
 *
 * 왜 필요한가?
 * - 클라이언트가 에러를 파싱하기 쉽게 구조화
 * - 디버깅 정보 제공 (timestamp, path 등)
 * - 일관된 에러 응답 형식
 *
 * 반환 예시:
 * {
 *   "timestamp": "2025-01-19T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "사용자를 찾을 수 없습니다. ID: 999",
 *   "path": "/api/users/999"
 * }
 */
public record ErrorResponse(
        LocalDateTime timestamp,  // 에러 발생 시각
        int status,               // HTTP 상태 코드
        String error,             // 상태 코드 설명 (예: "Not Found")
        String message,           // 구체적인 에러 메시지
        String path               // 에러가 발생한 API 경로
) {
    /**
     * 간단한 생성자 - status와 message만 필수
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path);
    }

    /**
     * 최소 정보로 생성 (path 없이)
     */
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, null);
    }
}
