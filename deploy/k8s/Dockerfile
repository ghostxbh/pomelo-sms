FROM openjdk:8-alpine

RUN rm -f /etc/localtime \
&& ln -sv /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
&& echo "Asia/Shanghai" > /etc/timezone

RUN mkdir -p /opt

ADD ./pomelo-sms-admin/target/pomelo-sms-admin.jar /opt/

WORKDIR /opt

ENTRYPOINT ["java", "-jar", "/opt/pomelo-sms-admin.jar"]
