#!/bin/bash
# вызывать в /etc/rc.local (при старте системы) sudo sh /home/denis/time-report-app/entrypoint.sh

server_bot_dir='/home/denis/time-report-app'
version=$(cat ${server_bot_dir}/pom.xml | grep \<tg-bot.version\> | cut -d '>' -f2 | cut -d '<' -f1)

# поднимаем бота
cd ${server_bot_dir}/tg-bot
java -jar target/tg-bot-${version}.jar > /dev/null 2>&1 &

# поднимаем веб-гуи
cd ${server_bot_dir}/gui-client
java -jar target/gui-client-${version}.jar > /dev/null 2>&1 &
cd ${server_bot_dir}