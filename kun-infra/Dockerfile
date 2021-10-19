# Get base image
FROM miotechoss/kun-spark:0.7.0-rc1
# ssh
EXPOSE 22

USER root

ENV TINI_VERSION v0.19.0
ADD https://github.com/krallin/tini/releases/download/${TINI_VERSION}/tini /tini
RUN chmod +x /tini

WORKDIR /server/target

COPY dockerize-alpine-linux-amd64-v0.6.1.tar.gz /server/target/dockerize-alpine-linux-amd64-v0.6.1.tar.gz
RUN tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-v0.6.1.tar.gz \
    && rm dockerize-alpine-linux-amd64-v0.6.1.tar.gz

COPY build/libs/kun-infra-0.1.jar /server/target/kun-infra.jar
EXPOSE 8088

ADD docker-entrypoint.sh /
RUN chmod +x  /docker-entrypoint.sh

ENV APP_CONFIG_ENV local
ENTRYPOINT ["/tini", "--", "/docker-entrypoint.sh"]