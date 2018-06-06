#!/bin/bash

ps aux | grep websocket-server | grep -v grep | awk '{print $2}' | xargs kill -9
git pull
java -jar build/libs/websocket-server-boot-1.0.0.jar --package=com.codertianwei.websocket --env=prod & > /dev/null 2>&1
tail -f logs/server.log
