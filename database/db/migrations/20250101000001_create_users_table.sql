-- migrate:up
-- 사용자 테이블 생성
--
-- API 명세(OpenAPI)와 매핑:
-- - UserResponse 스키마와 컬럼이 일치
-- - id, name, email, age
--
-- 비즈니스 규칙:
-- - email은 unique (중복 불가)
-- - age는 0 이상

CREATE TABLE users (
    -- 기본 키
    id BIGSERIAL PRIMARY KEY,

    -- 사용자 정보
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    age INTEGER NOT NULL,

    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 제약 조건 이름 명시
ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);

ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

-- 컬럼 코멘트
COMMENT ON TABLE users IS '사용자 테이블';
COMMENT ON COLUMN users.id IS '사용자 고유 ID (자동 증가)';
COMMENT ON COLUMN users.name IS '사용자 이름';
COMMENT ON COLUMN users.email IS '이메일 주소 (로그인 ID, 중복 불가)';
COMMENT ON COLUMN users.age IS '나이';
COMMENT ON COLUMN users.created_at IS '생성 일시';
COMMENT ON COLUMN users.updated_at IS '수정 일시';


-- migrate:down
-- 테이블 삭제
DROP TABLE IF EXISTS users;
