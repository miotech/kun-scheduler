#!/usr/bin/env sh
SENTRY_DSN=${sentry_dsn} SENTRY_ENVIRONMENT=${env} SENTRY_RELEASE=${tag} java ${JVM_OPTS} -jar /server/target/app.jar -Dtag=${tag} -Denv=${env}
