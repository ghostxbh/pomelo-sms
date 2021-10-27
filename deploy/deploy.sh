#!/bin/bash
PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin:~/bin
export PATH
LANG=en_US.UTF-8

##############################################################################
# |                           柚子云短信平台部署                              | #
##############################################################################
# |                                                                        | #
# |  Copyright (c) 2021 柚子云(http://sms.pomelo.work) All rights reserved. | #
# |                                                                        | #
##############################################################################
# |                           author: ghostxbh                             | #
##############################################################################
rootPath=/web/server
appName=pomelo-sms
appPort=8110

if [ $(whoami) != "root" ];then
	echo "please execute script by root!"
	echo "请切换至root用户在执行安装！"
	exit 1
fi

start_app()
{
  echo "启动 ${appName}"
  cd ${rootPath}/${appName}

  mvn clean install -Dmaven.test.skip=true
  sleep 1

  mv ${rootPath}/${appName}/pomelo-sms-admin/target/pomelo-sms-admin.jar ${rootPath}/app/pomelo-sms

  cd ${rootPath}/app/pomelo-sms
  nohup java -jar --server.port=${appPort} pomelo-sms-admin.jar >pomelo-sms-run.log &

  pid=`ps -aux | grep "${appName}-admin.jar" | grep -v 'grep' | awk '{print $2}'`
  if [ -n "${pid}" ]; then
      echo "${pid}" >pomelo-sms-run.pid
      echo "${appName} pid: ${pid}"
  fi
}

stop_app()
{
  echo "停止 ${appName}"
  cd ${rootPath}/app/pomelo-sms
  pid=`cat pomelo-sms-run.pid`
  if [ -n "${pid}" ]; then
      kill -9 ${pid}
      echo "${appName} app is stop！"
  fi
}

reload_app()
{
  echo "重启 ${appName}"

  stop_app

  cd ${rootPath}/${appName}
  echo "updating..."
  git pull

  echo "======= last updated ======"
  git log | head -n 10
  echo "==========================="

  rm -rf ${rootPath}/app/pomelo-sms/pomelo-sms-admin.jar

  start_app
}

case $1 in
  'start')
    start_app
    ;;
  'stop')
    stop_app
    ;;
  'reload')
    reload_app
    ;;
  'log')
    cd ${rootPath}/app/pomelo-sms
    tail -f pomelo-sms-run.log
    ;;
  *)
    echo "Use deploy || start || stop || reload || log"
    ;;
esac