package com.example.springbasic.service;

import com.example.springbasic.exception.DuplicateEmailException;
import com.example.springbasic.model.Role;
import com.example.springbasic.model.User;
import com.example.springbasic.repository.UserRepository;
import com.example.springbasic.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 담당하는 Service
 *
 * 주요 기능:
 * 1. 회원가입 (register)
 * 2. 로그인 (login)
 * 3. 사용자 조회 (loadUserByUsername)
 *
 * 보안:
 * - 비밀번호는 BCrypt로 암호화하여 저장
 * - 로그인 성공 시 JWT 토큰 발급
 * - Spring Security와 통합
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 생성자 주입
     *
     * @param userRepository 사용자 Repository
     * @param passwordEncoder BCrypt 암호화 (SecurityConfig에서 Bean 주입)
     * @param jwtUtil JWT 유틸리티
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 회원가입
     *
     * 프로세스:
     * 1. 이메일 중복 체크
     * 2. 비밀번호 BCrypt 암호화
     * 3. User 엔티티 생성 (기본 권한: USER)
     * 4. DB 저장
     * 5. JWT 토큰 발급
     *
     * @param request 회원가입 요청 정보
     * @return 인증 응답 (JWT 토큰 포함)
     * @throws DuplicateEmailException 이메일 중복 시
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateEmailException(request.email());
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 3. User 엔티티 생성
        User user = User.createNew(User.CreateRequest.of(
                request.name(),
                request.email(),
                request.age()
        ));

        // 4. 암호화된 비밀번호와 권한 설정
        user.update(User.UpdateRequest.of(
                null,  // name (변경 안함)
                null,  // email (변경 안함)
                null,  // age (변경 안함)
                encodedPassword,  // 암호화된 비밀번호
                Role.USER  // 기본 권한
        ));

        // 5. DB 저장
        User savedUser = userRepository.save(user);

        // 6. JWT 토큰 발급
        String token = jwtUtil.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRole().name()
        );
    }

    /**
     * 로그인
     *
     * 프로세스:
     * 1. 이메일로 사용자 조회
     * 2. 비밀번호 검증 (BCrypt.matches)
     * 3. JWT 토큰 발급
     *
     * @param request 로그인 요청 정보
     * @return 인증 응답 (JWT 토큰 포함)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     * @throws BadCredentialsException 비밀번호가 일치하지 않을 때
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + request.email()
                ));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다");
        }

        // 3. JWT 토큰 발급
        String token = jwtUtil.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }

    /**
     * 이메일로 사용자 조회
     *
     * Spring Security의 UserDetailsService와 통합 가능
     *
     * @param email 이메일 (username)
     * @return User 엔티티 (UserDetails 구현)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "사용자를 찾을 수 없습니다: " + email
                ));
    }

    /**
     * 회원가입 요청 DTO
     *
     * @param name 이름
     * @param email 이메일
     * @param age 나이
     * @param password 평문 비밀번호 (암호화 전)
     */
    public record RegisterRequest(
            String name,
            String email,
            Integer age,
            String password
    ) {
        public static RegisterRequest of(String name, String email, Integer age, String password) {
            return new RegisterRequest(name, email, age, password);
        }
    }

    /**
     * 로그인 요청 DTO
     *
     * @param email 이메일
     * @param password 평문 비밀번호
     */
    public record LoginRequest(
            String email,
            String password
    ) {
        public static LoginRequest of(String email, String password) {
            return new LoginRequest(email, password);
        }
    }

    /**
     * 인증 응답 DTO
     *
     * @param token JWT 토큰
     * @param userId 사용자 ID
     * @param email 이메일
     * @param name 이름
     * @param role 권한 (USER, ADMIN)
     */
    public record AuthResponse(
            String token,
            Long userId,
            String email,
            String name,
            String role
    ) {
    }
}
