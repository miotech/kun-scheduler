FROM openjdk:8u181-jdk-alpine

RUN apk add --no-cache tini

WORKDIR /server/target

COPY dockerize-alpine-linux-amd64-v0.6.1.tar.gz /server/target/dockerize-alpine-linux-amd64-v0.6.1.tar.gz
RUN tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-v0.6.1.tar.gz \
    && rm dockerize-alpine-linux-amd64-v0.6.1.tar.gz

COPY kun-security-server/build/libs/kun-security-server.jar /server/target/
EXPOSE 8080

ADD docker-entrypoint.sh /
RUN chmod +x  /docker-entrypoint.sh

ENV APP_CONFIG_ENV local
ENTRYPOINT ["/sbin/tini", "--", "/docker-entrypoint.sh"]