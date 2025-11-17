#!/bin/bash
# DBML을 SQL로 변환 (전체 스키마)
#
# 사용법:
#   ./scripts/dbml-to-sql.sh
#
# 출력:
#   generated/schema.sql

set -e

# 현재 디렉토리 확인
if [[ ! -f "schema.dbml" ]]; then
    echo "❌ schema.dbml 파일을 찾을 수 없습니다."
    echo "database/ 폴더에서 실행해주세요."
    exit 1
fi

# dbml2sql 확인
if ! command -v dbml2sql &> /dev/null; then
    echo "❌ dbml2sql이 설치되어 있지 않습니다."
    echo ""
    echo "설치 방법:"
    echo "  ./scripts/install-dbml-cli.sh"
    echo ""
    echo "또는:"
    echo "  npm install -g @dbml/cli"
    exit 1
fi

# 출력 디렉토리 생성
mkdir -p generated

echo "🔄 DBML → SQL 변환 중..."
echo ""

# DBML을 PostgreSQL SQL로 변환
dbml2sql schema.dbml --postgres > generated/schema.sql

echo "✅ 변환 완료!"
echo ""
echo "📄 생성된 파일: generated/schema.sql"
echo ""
echo "📋 다음 단계:"
echo "1. generated/schema.sql 파일 확인"
echo "2. 필요한 부분을 dbmate 마이그레이션으로 복사"
echo ""
echo "예시:"
echo "  # 새 마이그레이션 생성"
echo "  dbmate new add_posts_table"
echo ""
echo "  # generated/schema.sql에서 필요한 SQL 복사"
echo "  # → db/migrations/xxx_add_posts_table.sql의 -- migrate:up에 붙여넣기"
