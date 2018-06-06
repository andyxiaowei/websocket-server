#!/bin/bash

./gradlew clean build
ps aux | grep websocket-server | grep -v grep | awk '{print $2}' | xargs kill -9
java -jar build/libs/websocket-server-boot-1.0.0.jar --package=com.codertianwei.websocket --env=dev & > /dev/null 2>&1
tail -f logs/server.log
