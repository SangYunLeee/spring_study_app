-- migrate:up
-- 댓글 테이블 생성
-- JPA 연관관계:
--   - User (N:1) - 작성자
--   - Post (N:1) - 게시글

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 외래 키 제약조건: User와의 관계
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    -- 외래 키 제약조건: Post와의 관계
    -- ON DELETE CASCADE: Post 삭제 시 해당 Post의 모든 Comment도 삭제
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE
);

-- 인덱스: 게시글별 댓글 조회 최적화
CREATE INDEX idx_comments_post_id ON comments(post_id);

-- 인덱스: 작성자별 댓글 조회 최적화
CREATE INDEX idx_comments_user_id ON comments(user_id);

-- 인덱스: 특정 게시글의 최신 댓글 조회 최적화
CREATE INDEX idx_comments_post_created ON comments(post_id, created_at DESC);

-- 복합 인덱스: 특정 사용자가 특정 게시글에 작성한 댓글 조회
CREATE INDEX idx_comments_post_user ON comments(post_id, user_id);

COMMENT ON TABLE comments IS '댓글 정보';
COMMENT ON COLUMN comments.id IS '댓글 ID (자동 증가)';
COMMENT ON COLUMN comments.content IS '댓글 내용';
COMMENT ON COLUMN comments.user_id IS '작성자 ID (users 테이블 참조)';
COMMENT ON COLUMN comments.post_id IS '게시글 ID (posts 테이블 참조)';
COMMENT ON COLUMN comments.created_at IS '생성 시각';
COMMENT ON COLUMN comments.updated_at IS '수정 시각';

-- migrate:down
DROP TABLE IF EXISTS comments CASCADE;
