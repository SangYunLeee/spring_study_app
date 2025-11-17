-- migrate:up
-- 이메일 검색 성능 향상을 위한 인덱스 추가
--
-- 용도:
-- - GET /api/users/search/email?email=xxx API 성능 향상
-- - 로그인 시 이메일 조회 속도 개선
--
-- 참고:
-- - unique 제약조건이 있어도 명시적 인덱스 추가로 검색 최적화

CREATE INDEX idx_users_email ON users(email);

COMMENT ON INDEX idx_users_email IS '이메일 검색 성능 향상';


-- migrate:down
-- 인덱스 삭제
DROP INDEX IF EXISTS idx_users_email;
