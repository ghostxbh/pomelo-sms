# sms-system 短信平台

## framework
### sms_platform
+ springboot
+ mysql
+ thymeleaf
+ bootstrap

## deploy
- 1.执行克隆：`git clone <url>` 
- 2.执行数据库sql，`mobile.sql`
- 3.修改数据库连接，`mobileproject/src/main/resources/application.properties`
- 4.执行打包脚本，`mvn clean install`
- 5.运行脚本，`nohup java -jar /target/mobileproject.jar &`
- 6.访问测试[短信平台](http://127.0.0.1:8888)
