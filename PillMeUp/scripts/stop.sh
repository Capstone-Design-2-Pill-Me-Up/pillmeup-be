#!/bin/bash
echo "> 현재 실행 중인 애플리케이션 pid 확인"
CURRENT_PID=$(pgrep -f 'java -jar')

if [ -z "$CURRENT_PID" ]; then
  echo "> 현재 구동 중인 애플리케이션이 없습니다."
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi
