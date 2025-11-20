package com.example.springbasic.security;

import com.example.springbasic.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 *
 * 역할:
 * - 매 요청마다 실행 (OncePerRequestFilter)
 * - HTTP Header에서 JWT 토큰 추출
 * - 토큰 검증
 * - 유효하면 SecurityContext에 인증 정보 저장
 *
 * 동작 순서:
 * 1. Authorization Header 확인
 * 2. "Bearer " 접두사 확인
 * 3. JWT 토큰 추출
 * 4. 토큰에서 사용자명(이메일) 추출
 * 5. 사용자 조회
 * 6. 토큰 유효성 검증
 * 7. SecurityContext에 인증 정보 저장
 * 8. 다음 필터로 진행
 *
 * Spring Security 필터 체인:
 * Request → JwtAuthenticationFilter → 기타 필터들 → Controller
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final AuthService authService;

    /**
     * Authorization Header 키
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer 토큰 접두사
     */
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    /**
     * 필터 메인 로직
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 다음 필터 체인
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 1. Authorization Header에서 JWT 토큰 추출
            String jwt = extractJwtFromRequest(request);

            // 2. 토큰이 있고, SecurityContext에 인증 정보가 없으면 처리
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 3. 토큰에서 사용자명(이메일) 추출
                String email = jwtUtil.extractUsername(jwt);

                // 4. 사용자 조회
                UserDetails userDetails = authService.loadUserByUsername(email);

                // 5. 토큰 유효성 검증
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // 6. 인증 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,           // Principal (인증된 사용자)
                                    null,                  // Credentials (비밀번호는 불필요)
                                    userDetails.getAuthorities()  // 권한 목록
                            );

                    // 7. 요청 정보 추가 (IP, Session 등)
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 8. SecurityContext에 인증 정보 저장
                    // 이후 Controller에서 @AuthenticationPrincipal로 접근 가능
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // JWT 검증 실패 시 로그만 남기고 계속 진행
            // (인증 실패는 다음 필터에서 처리)
            logger.error("JWT 인증 중 오류 발생: " + e.getMessage());
        }

        // 9. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP Request에서 JWT 토큰 추출
     *
     * Authorization Header 형식:
     * "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWI..."
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (Bearer 접두사 제거), 없으면 null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // "Bearer " 로 시작하면 토큰 추출
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
