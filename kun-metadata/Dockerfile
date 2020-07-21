FROM hub.miotech.com/library/openjdk:8u181-jdk

ARG sentry_dsn
ARG sentry_env
ARG sentry_release

COPY build/libs/kun-metadata-web-1.0.jar /server/target/
WORKDIR /server/target
EXPOSE 8084

CMD SENTRY_DSN=${sentry_dsn} SENTRY_ENVIRONMENT=${env} SENTRY_RELEASE=${tag} java ${JVM_OPTS} -jar /server/target/kun-metadata-web-1.0.jar -Dapp=kun-metadata -Dtag=${tag} -Denv=${env}
