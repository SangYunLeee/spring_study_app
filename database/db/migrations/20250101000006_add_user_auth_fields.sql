-- migrate:up
-- User 테이블에 인증 관련 필드 추가

-- password: BCrypt로 암호화된 비밀번호 저장
ALTER TABLE users
    ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '$2a$10$defaultHashForMigration';

-- role: 사용자 권한 (USER, ADMIN)
ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- 인덱스 추가 (role 기반 조회 최적화)
CREATE INDEX idx_users_role ON users(role);

-- 컬럼 코멘트
COMMENT ON COLUMN users.password IS 'BCrypt 암호화된 비밀번호';
COMMENT ON COLUMN users.role IS '사용자 권한 (USER, ADMIN)';


-- migrate:down
-- 롤백: 추가된 필드 제거

DROP INDEX IF EXISTS idx_users_role;

ALTER TABLE users
    DROP COLUMN IF EXISTS password;

ALTER TABLE users
    DROP COLUMN IF EXISTS role;