#!/bin/bash
echo "> 새 애플리케이션 배포 시작"

REPOSITORY=/home/ec2-user/app
cd $REPOSITORY

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)
echo "> 실행할 JAR 파일: $JAR_NAME"

chmod +x $JAR_NAME

echo "> $JAR_NAME 실행 (Java 21)"
nohup java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &
