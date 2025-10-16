#!/bin/bash

REPOSITORY=/home/ubuntu/app

# === Stop Logic ===
stop_app() {
    echo "Stopping the existing application..."

    CURRENT_PID=$(pgrep -f "app.jar")

    if [ -z "$CURRENT_PID" ]; then
        echo "No running application to stop."
    else
        echo "Killing process $CURRENT_PID"
        kill -15 $CURRENT_PID
        sleep 5
    fi
}

# === Start Logic ===
start_app() {
    CONFIG_FILE="$REPOSITORY/rds-config.env"

    JAR_PATH="$REPOSITORY/app.jar"

    # RDS 설정 파일 로드
    if [ -f "$CONFIG_FILE" ]; then
        echo "Loading RDS configuration from $CONFIG_FILE"
        source "$CONFIG_FILE"
    else
        echo "Error: RDS config file not found at $CONFIG_FILE"
        exit 1
    fi

    echo "Starting new application deployment: $JAR_PATH"

    # 애플리케이션 실행
    nohup java -jar \
        -Dspring.profiles.active=prod \
        $JAR_PATH > $REPOSITORY/app.log 2>&1 &

    echo "New application deployment complete."
}

if [ "$LIFECYCLE_EVENT" == "ApplicationStop" ]; then
    stop_app
elif [ "$LIFECYCLE_EVENT" == "ApplicationStart" ]; then
    start_app
fi