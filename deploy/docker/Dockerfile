FROM openjdk:8-alpine

RUN echo "Asia/Shanghai" > /etc/timezone

# Required for starting application up.
RUN apk update && apk add /bin/sh

RUN mkdir -p /opt/app
ENV PROJECT_HOME /opt/app

COPY pomelo-sms-admin/target/pomelo-sms-admin.jar $PROJECT_HOME/pomelo-sms-admin.jar

WORKDIR $PROJECT_HOME

CMD ["java","-jar","./pomelo-sms-admin.jar"]