#!/bin/bash
source /etc/profile

path=$1
name=sms-system
jar="$name.jar"

src=$path/$name
app=$src/target

cd $src

echo "拉取新代码"
git pull

echo "以下为最新推送日志:"
echo "======= 最近更新 ======"
git log | head -n 10
echo "======================"

mvn clean package

port=`ps -ef | grep "$jar" | grep -v 'grep' | awk '{print $2}'`

echo "kill $port"
if [ -n "$port" ]; then
kill -9 $port
fi

cd $app

nohup java -jar $app/$jar &

sleep 1s

ps -ef | grep $jar | grep -v 'grep' | awk '{print $2}'

echo "启动 $name 成功"
