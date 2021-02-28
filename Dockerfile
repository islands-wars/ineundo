FROM openjdk:15-alpine

MAINTAINER Xharos <xharos@islandswars.fr>

WORKDIR /usr/local/is/velocity

COPY serv/velocity.jar velocity.jar

COPY serv/velocity.toml velocity.toml

COPY build/libs/* plugins/

CMD java ${JVM_ARGS} -jar velocity.jar