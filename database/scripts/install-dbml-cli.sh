#!/bin/bash
# DBML CLI 설치 스크립트
#
# DBML (Database Markup Language) CLI를 설치합니다.
# dbdiagram.io에서 사용하는 형식입니다.

set -e

echo "🔧 DBML CLI 설치 중..."

# Node.js 확인
if ! command -v node &> /dev/null; then
    echo "❌ Node.js가 설치되어 있지 않습니다."
    echo "다음 중 하나를 선택하세요:"
    echo ""
    echo "1. Homebrew로 설치:"
    echo "   brew install node"
    echo ""
    echo "2. 공식 사이트에서 다운로드:"
    echo "   https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js 발견: $(node --version)"

# npm 확인
if ! command -v npm &> /dev/null; then
    echo "❌ npm이 설치되어 있지 않습니다."
    exit 1
fi

echo "✅ npm 발견: $(npm --version)"

# @dbml/cli 설치
echo ""
echo "📦 @dbml/cli 설치 중..."
npm install -g @dbml/cli

echo ""
echo "✅ DBML CLI 설치 완료!"
echo ""
echo "사용법:"
echo "  dbml2sql schema.dbml --postgres > output.sql"
echo "  sql2dbml database.sql --postgres > schema.dbml"
