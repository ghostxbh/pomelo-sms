FROM openjdk:8-alpine

RUN echo "Asia/Shanghai" > /etc/timezone

# Required for starting application up.
RUN apk update && apk add /bin/sh

RUN mkdir -p /opt/app
ENV PROJECT_HOME /opt/app

COPY target/sms-system.jar $PROJECT_HOME/sms-system.jar

WORKDIR $PROJECT_HOME

CMD ["java","-jar","./sms-system.jar"]