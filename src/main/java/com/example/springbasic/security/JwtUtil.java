package com.example.springbasic.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 토큰 생성 및 검증 유틸리티
 *
 * JWT (JSON Web Token):
 * - 사용자 인증 정보를 안전하게 전달하는 토큰 기반 인증 방식
 * - 구조: Header.Payload.Signature
 * - Stateless: 서버에 세션 저장 없이 토큰만으로 인증
 *
 * 주요 기능:
 * 1. 토큰 생성 (generateToken)
 * 2. 토큰 검증 (validateToken)
 * 3. 토큰에서 정보 추출 (extractUsername, extractExpiration 등)
 *
 * 보안:
 * - HMAC-SHA256 알고리즘 사용
 * - Secret Key는 환경 변수나 application.yml에서 주입
 * - 토큰 만료 시간 설정 (기본 24시간)
 */
@Component
public class JwtUtil {

    /**
     * JWT 서명에 사용할 비밀 키
     *
     * application.yml에서 주입:
     * jwt:
     *   secret: your-256-bit-secret-key-here
     *
     * 주의사항:
     * - 최소 256비트 (32자) 이상 권장
     * - 프로덕션 환경에서는 환경 변수로 관리
     * - Git에 절대 커밋하지 말 것!
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * JWT 토큰 만료 시간 (밀리초)
     *
     * application.yml에서 주입:
     * jwt:
     *   expiration: 86400000  # 24시간 (1000 * 60 * 60 * 24)
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 비밀 키 생성
     * - HMAC-SHA256 알고리즘에 사용
     * - Secret 문자열을 바이트로 변환하여 SecretKey 생성
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰에서 사용자명 추출
     *
     * @param token JWT 토큰
     * @return 사용자명 (이메일)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * JWT 토큰에서 만료 시간 추출
     *
     * @param token JWT 토큰
     * @return 만료 시간
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * JWT 토큰에서 특정 Claim 추출
     *
     * @param token JWT 토큰
     * @param claimsResolver Claim 추출 함수
     * @return 추출된 Claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * JWT 토큰에서 모든 Claim 추출
     *
     * @param token JWT 토큰
     * @return 모든 Claim
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWT 토큰 만료 여부 확인
     *
     * @param token JWT 토큰
     * @return true: 만료됨, false: 유효함
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * JWT 토큰 생성 (UserDetails 기반)
     *
     * @param userDetails Spring Security UserDetails
     * @return 생성된 JWT 토큰
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * JWT 토큰 생성 (사용자명 기반)
     *
     * @param username 사용자명 (이메일)
     * @return 생성된 JWT 토큰
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * JWT 토큰 생성 (추가 Claim 포함)
     *
     * @param extraClaims 추가 Claim (role, permissions 등)
     * @param userDetails Spring Security UserDetails
     * @return 생성된 JWT 토큰
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * JWT 토큰 생성 (실제 구현)
     *
     * JWT 구조:
     * - Header: 알고리즘 정보 (HMAC-SHA256)
     * - Payload: Claims (사용자 정보, 만료 시간 등)
     * - Signature: Header + Payload를 Secret Key로 서명
     *
     * @param claims 추가 Claim
     * @param subject 사용자명 (이메일)
     * @return 생성된 JWT 토큰
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)                    // 추가 Claim
                .subject(subject)                  // 사용자명 (이메일)
                .issuedAt(now)                     // 발급 시간
                .expiration(expirationDate)        // 만료 시간
                .signWith(getSigningKey())         // 서명
                .compact();                        // 문자열로 변환
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * 검증 항목:
     * 1. 토큰에서 추출한 사용자명과 UserDetails의 사용자명이 일치하는가?
     * 2. 토큰이 만료되지 않았는가?
     *
     * @param token JWT 토큰
     * @param userDetails Spring Security UserDetails
     * @return true: 유효함, false: 유효하지 않음
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * JWT 토큰 유효성 검증 (사용자명 기반)
     *
     * @param token JWT 토큰
     * @param username 사용자명 (이메일)
     * @return true: 유효함, false: 유효하지 않음
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * JWT 토큰 파싱 가능 여부 확인 (형식 검증)
     *
     * 예외 처리:
     * - MalformedJwtException: 잘못된 형식
     * - ExpiredJwtException: 만료된 토큰
     * - UnsupportedJwtException: 지원하지 않는 토큰
     * - IllegalArgumentException: 빈 토큰
     *
     * @param token JWT 토큰
     * @return true: 파싱 가능, false: 파싱 불가
     */
    public Boolean canParseToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            // 잘못된 JWT 형식
            return false;
        } catch (ExpiredJwtException e) {
            // 만료된 토큰 (만료된 토큰도 파싱은 가능하지만 유효하지 않음)
            return false;
        } catch (UnsupportedJwtException e) {
            // 지원하지 않는 JWT
            return false;
        } catch (IllegalArgumentException e) {
            // 빈 토큰
            return false;
        }
    }
}