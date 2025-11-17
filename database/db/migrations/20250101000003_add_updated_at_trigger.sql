-- migrate:up
-- updated_at 자동 업데이트 트리거 생성
--
-- 용도:
-- - updated_at: 레코드 수정 시간 (자동)
--
-- 향후 활용:
-- - 감사(Audit) 로그
-- - 데이터 분석
-- - API 응답에 포함 가능

-- updated_at 자동 업데이트 함수
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- users 테이블에 트리거 적용
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON FUNCTION update_updated_at_column() IS 'updated_at 자동 업데이트 함수';


-- migrate:down
-- 트리거 및 함수 삭제
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
DROP FUNCTION IF EXISTS update_updated_at_column();
