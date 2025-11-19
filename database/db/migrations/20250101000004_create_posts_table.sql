-- migrate:up
-- 게시글 테이블 생성
-- JPA 연관관계: User (N:1) - 작성자

CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 외래 키 제약조건: User와의 관계
    -- ON DELETE CASCADE: User 삭제 시 해당 User의 모든 Post도 삭제
    -- 실무에서는 ON DELETE SET NULL 또는 soft delete 고려
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- 인덱스: 작성자별 게시글 조회 최적화
CREATE INDEX idx_posts_user_id ON posts(user_id);

-- 인덱스: 최신 게시글 조회 최적화
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- 복합 인덱스: 특정 사용자의 최신 게시글 조회 최적화
CREATE INDEX idx_posts_user_created ON posts(user_id, created_at DESC);

COMMENT ON TABLE posts IS '게시글 정보';
COMMENT ON COLUMN posts.id IS '게시글 ID (자동 증가)';
COMMENT ON COLUMN posts.title IS '게시글 제목';
COMMENT ON COLUMN posts.content IS '게시글 내용';
COMMENT ON COLUMN posts.user_id IS '작성자 ID (users 테이블 참조)';
COMMENT ON COLUMN posts.created_at IS '생성 시각';
COMMENT ON COLUMN posts.updated_at IS '수정 시각';

-- migrate:down
DROP TABLE IF EXISTS posts CASCADE;
