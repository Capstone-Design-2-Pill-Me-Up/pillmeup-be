#!/bin/bash

REPOSITORY=/home/ubuntu/app
PROJECT_NAME=pillmeup

CURRENT_PID=$(pgrep -f ${PROJECT_NAME}*.jar)

echo "실행 중인 애플리케이션 PID: $CURRENT_PID"

if [ -z "$CURRENT_PID" ]; then
    echo "현재 실행 중인 애플리케이션이 없습니다."
else
    echo "kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "새 애플리케이션 배포 시작"

JAR_NAME=$(ls -tr $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)

echo "JAR Name: $JAR_NAME"

# 백그라운드에서 Spring Boot 애플리케이션 실행
# 로그 파일은 app.log에 기록
nohup java -jar $REPOSITORY/build/libs/$JAR_NAME > $REPOSITORY/app.log 2>&1 &

echo "새 애플리케이션 배포 완료"