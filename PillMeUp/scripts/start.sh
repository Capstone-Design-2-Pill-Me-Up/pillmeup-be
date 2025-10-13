#!/bin/bash
DEPLOY_PATH=/home/ubuntu/app
JAR_FILE=$(ls -tr $DEPLOY_PATH/*.jar | tail -n 1) # 최신 jar 파일 이름 자동 감지

echo "> 배포 파일: $JAR_FILE"
echo "> 애플리케이션을 prod 프로필(H2)로 백그라운드 실행"

# H2 DB 사용을 위해 prod 프로필을 명시적으로 사용합니다.
nohup java -jar -Dspring.profiles.active=prod "$JAR_FILE" > $DEPLOY_PATH/nohup.out 2>&1 &