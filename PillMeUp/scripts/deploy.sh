#!/bin/bash

REPOSITORY=/home/ubuntu/app
CONFIG_FILE="$REPOSITORY/rds-config.env"
PROJECT_NAME=pillmeup

JAR_PATH="$REPOSITORY/PillMeUp/build/libs"
JAR_NAME=$(ls -tr $JAR_PATH/ | grep 'SNAPSHOT.jar' | tail -n 1)

if [ -f "$CONFIG_FILE" ]; then
    echo "Loading RDS configuration from $CONFIG_FILE"
    source "$CONFIG_FILE"
fi

CURRENT_PID=$(pgrep -f ${PROJECT_NAME}*.jar)

echo "실행 중인 애플리케이션 PID: $CURRENT_PID"

if [ -z "$CURRENT_PID" ]; then
    echo "현재 실행 중인 애플리케이션이 없습니다."
else
    echo "kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "새 애플리케이션 배포 시작": $JAR_NAME"

nohup java -jar \
    -Dspring.profiles.active=prod \
    -Dspring.datasource.url="$RDS_URL" \
    -Dspring.datasource.username="$RDS_USERNAME" \
    -Dspring.datasource.password="$RDS_PASSWORD" \
    $JAR_PATH/$JAR_NAME > $REPOSITORY/app.log 2>&1 &

echo "새 애플리케이션 배포 완료"