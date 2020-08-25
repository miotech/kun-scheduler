FROM hub.miotech.com/library/openjdk:8u181-jdk
ARG sentry_dsn
ARG sentry_env
ARG sentry_release

RUN apk add --no-cache tini

COPY build/libs/kun-data-platform.jar /server/target/app.jar
WORKDIR /server/target
EXPOSE 8088

ADD docker-entrypoint.sh /
RUN chmod +x  /docker-entrypoint.sh

ENV APP_CONFIG_ENV dev
ENTRYPOINT ["/sbin/tini", "--", "/docker-entrypoint.sh"]