#!/bin/bash

# 변수 설정
APP_DIR="/home/ubuntu/app"
JAR_NAME="app.jar"
JAR_PATH="$APP_DIR/$JAR_NAME"

# 기존 애플리케이션 중지
echo ">> 기존 애플리케이션을 중지합니다..."
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo ">> 현재 실행 중인 애플리케이션이 없습니다."
else
    echo ">> 프로세스 종료: $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

# 새 애플리케이션 시작
echo ">> 새 애플리케이션을 시작합니다..."

# Spring Boot는 EC2에 설정된 환경 변수(DB_PASSWORD 등)를 자동으로 인식합니다.
nohup java -jar \
    -Dspring.profiles.active=prod \
    $JAR_PATH > $APP_DIR/app.log 2>&1 &

echo ">> 애플리케이션이 시작되었습니다."