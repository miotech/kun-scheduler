FROM openjdk:8u181-jdk-alpine

RUN apk add --no-cache tini

WORKDIR /server/target

COPY dockerize-alpine-linux-amd64-v0.6.1.tar.gz /server/target/dockerize-alpine-linux-amd64-v0.6.1.tar.gz
RUN tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-v0.6.1.tar.gz \
    && rm dockerize-alpine-linux-amd64-v0.6.1.tar.gz

COPY build/libs/kubernetesOperatorLauncher.jar /server/target/kubernetesOperatorLauncher.jar
COPY src/resources/NopOperator.jar /server/libs/NopOperator.jar