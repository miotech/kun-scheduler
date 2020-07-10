FROM hub.miotech.com/library/openjdk:8u181-jdk
ARG sentry_dsn
ARG sentry_env
ARG sentry_release

COPY build/libs/kun-data-discover.jar /server/target/
WORKDIR /server/target
EXPOSE 8080

CMD SENTRY_DSN=${sentry_dsn} SENTRY_ENVIRONMENT=${env} SENTRY_RELEASE=${tag} java ${JAVA_OPTS} -jar /server/target/kun-data-discovery.jar -Dapp=kun-data-discovery-server -Dtag=${tag} -Denv=${env}