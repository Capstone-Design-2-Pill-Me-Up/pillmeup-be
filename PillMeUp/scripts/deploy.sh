#!/bin/bash

# 프로젝트 디렉토리 경로
REPOSITORY=/home/ubuntu/app
# 실행할 JAR 파일 이름
JAR_NAME="app.jar"
JAR_PATH="$REPOSITORY/$JAR_NAME"

# 1. 기존 애플리케이션 중지
echo ">> Stop existing application..."
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo ">> No running application to stop."
else
    echo ">> Killing process: $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

# 2. 새 애플리케이션 배포
echo ">> Start new application..."

nohup java -jar \
    -Dspring.profiles.active=prod \
    $JAR_PATH > $REPOSITORY/app.log 2>&1 &

echo ">> Application started."