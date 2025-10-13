#!/bin/bash
APP_NAME=PillMeUp-0.0.1-SNAPSHOT.jar
# 애플리케이션 이름은 프로젝트명에 따라 정확히 수정

PID=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
    echo "> 실행 중인 애플리케이션 없음. 종료하지 않음."
else
    echo "> PID $PID 확인됨. kill -9 $PID"
    kill -9 "$PID"
fi