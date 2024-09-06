#!/bin/bash

# SQLite 파일 목록
SQLITE_FILES=("key.sqlite" "objectkv.sqlite")

# 특정 SQLite 파일의 모든 테이블 삭제
delete_tables_for_file() {
    local file_path="../$1"
    if [ -f "$file_path" ]; then
        echo "처리 중: $1"
        
        # 테이블 목록 출력
        echo "삭제 전 테이블 목록:"
        sqlite3 "$file_path" "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';"
        
        # 각 테이블에 대해 개별적으로 DROP 명령 실행
        tables=$(sqlite3 "$file_path" "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';")
        for table in $tables; do
            echo "테이블 삭제 중: $table"
            sqlite3 "$file_path" <<EOF
            PRAGMA foreign_keys = OFF;
            DROP TABLE IF EXISTS $table;
            PRAGMA foreign_keys = ON;
EOF
            if [ $? -ne 0 ]; then
                echo "오류: $table 테이블 삭제 실패"
            fi
        done
        
        # VACUUM 실행
        echo "VACUUM 실행 중..."
        sqlite3 "$file_path" "VACUUM;"
        
        # 삭제 후 테이블 목록 확인
        echo "삭제 후 테이블 목록:"
        sqlite3 "$file_path" "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';"
        
        echo "$1의 테이블 삭제 작업이 완료되었습니다."
    else
        echo "경고: $1 파일을 찾을 수 없습니다."
    fi
}

# 메인 실행 로직
main() {
    for file in "${SQLITE_FILES[@]}"; do
        delete_tables_for_file "$file"
    done

    echo "모든 작업이 완료되었습니다."
}

# 스크립트 실행
main