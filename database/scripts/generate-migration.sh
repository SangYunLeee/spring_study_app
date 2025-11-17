#!/bin/bash
# DBML에서 dbmate 마이그레이션 생성
#
# 사용법:
#   ./scripts/generate-migration.sh "description"
#
# 예시:
#   ./scripts/generate-migration.sh "add_posts_table"

set -e

# 현재 디렉토리 확인
if [[ ! -f "schema.dbml" ]]; then
    echo "❌ schema.dbml 파일을 찾을 수 없습니다."
    echo "database/ 폴더에서 실행해주세요."
    exit 1
fi

# 인자 확인
if [ -z "$1" ]; then
    echo "❌ 마이그레이션 설명을 입력해주세요."
    echo ""
    echo "사용법:"
    echo "  ./scripts/generate-migration.sh \"description\""
    echo ""
    echo "예시:"
    echo "  ./scripts/generate-migration.sh \"add_posts_table\""
    exit 1
fi

DESCRIPTION=$1

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

echo "🔄 DBML → SQL 변환 중..."

# DBML을 SQL로 변환
dbml2sql schema.dbml --postgres > /tmp/schema_from_dbml.sql

echo "✅ SQL 생성 완료"
echo ""

# dbmate로 새 마이그레이션 생성
echo "📝 새 마이그레이션 파일 생성 중..."

# dbmate 확인
if ! command -v dbmate &> /dev/null && ! command -v ~/bin/dbmate &> /dev/null; then
    echo "❌ dbmate가 설치되어 있지 않습니다."
    echo ""
    echo "설치 방법은 database/README.md를 참조하세요."
    exit 1
fi

# dbmate 경로 설정
DBMATE_CMD="dbmate"
if command -v ~/bin/dbmate &> /dev/null; then
    DBMATE_CMD="~/bin/dbmate"
fi

# 새 마이그레이션 파일 생성
MIGRATION_FILE=$($DBMATE_CMD new "$DESCRIPTION" | grep -o 'db/migrations/.*\.sql')

echo "✅ 마이그레이션 파일 생성: $MIGRATION_FILE"
echo ""
echo "📋 다음 단계:"
echo "1. $MIGRATION_FILE 파일 편집"
echo "2. -- migrate:up 섹션에 SQL 추가"
echo "3. -- migrate:down 섹션에 롤백 SQL 추가"
echo "4. dbmate up 실행"
echo ""
echo "💡 TIP: /tmp/schema_from_dbml.sql 파일을 참고하세요"
