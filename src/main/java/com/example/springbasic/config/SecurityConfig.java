package com.example.springbasic.config;

import com.example.springbasic.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 *
 * 주요 설정:
 * 1. PasswordEncoder: BCrypt 암호화
 * 2. SecurityFilterChain: 인증/인가 규칙
 * 3. JWT 기반 인증 (Stateless)
 * 4. CSRF 비활성화 (REST API)
 *
 * JWT 방식:
 * - Session 사용 안함 (STATELESS)
 * - JWT 토큰으로 인증
 * - 매 요청마다 토큰 검증
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // @PreAuthorize, @Secured 어노테이션 활성화
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 생성자 주입
     *
     * @param jwtAuthenticationFilter JWT 인증 필터
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 비밀번호 암호화 Bean
     *
     * BCrypt:
     * - Salt를 자동으로 생성하여 암호화
     * - 같은 비밀번호도 매번 다른 해시값 생성
     * - 일방향 암호화 (복호화 불가)
     * - 강도 조절 가능 (기본 10)
     *
     * 사용 예:
     * String encoded = passwordEncoder.encode("myPassword123");
     * boolean matches = passwordEncoder.matches("myPassword123", encoded);
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인 설정
     *
     * 필터 체인:
     * 클라이언트 요청 → JwtAuthenticationFilter → 기타 필터들 → Controller
     *
     * 설정 항목:
     * 1. CSRF 보호: 비활성화 (REST API는 불필요)
     * 2. Session: 비활성화 (JWT 사용)
     * 3. 인증/인가 규칙
     * 4. JWT 필터 추가
     *
     * @param http HttpSecurity 설정 객체
     * @return SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ========== CSRF 비활성화 ==========
                // REST API는 CSRF 토큰이 불필요
                // (Session을 사용하지 않으므로)
                .csrf(AbstractHttpConfigurer::disable)

                // ========== Session 비활성화 ==========
                // JWT 방식은 Session을 사용하지 않음 (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ========== 인증/인가 규칙 ==========
                .authorizeHttpRequests(auth -> auth
                        // 공개 API (인증 불필요)
                        .requestMatchers(
                                "/api/auth/**",      // 회원가입, 로그인
                                "/swagger-ui/**",    // Swagger UI
                                "/api-docs/**",      // OpenAPI 문서
                                "/v3/api-docs/**"    // Swagger v3
                        ).permitAll()

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // ========== JWT 인증 필터 추가 ==========
                // UsernamePasswordAuthenticationFilter 전에 실행
                // JWT 토큰을 먼저 검증한 후, 다음 필터로 진행
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
